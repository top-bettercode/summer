package top.bettercode.summer.tools.optimal.entity

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.springframework.core.io.ClassPathResource
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.excel.ExcelField
import top.bettercode.summer.tools.excel.ExcelImport
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.optimal.*
import top.bettercode.summer.tools.optimal.form.ComponentsForm
import top.bettercode.summer.tools.optimal.form.MaterialPriceForm
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.*
import java.util.stream.Collectors

/**
 * @author Peter Wu
 */
class ReqData(
        /** 原料名称  */
        var productName: String) {
    // --------------------------------------------

    /** 原料最小使用量，单位KG  */
    var minMaterialWeight = 0.0

    /** 目标重量，单位KG  */
    var targetWeight: Double = 1000.0

    /** 原料进料口最大数，-1不限  */
    var numMaxMaterials = -1
        get() = if (field <= 0) materials.size else field

    /** 最大结果数  */
    var maxResult = 20

    /** 超时时间  */
    var timeout = 30
    // --------------------------------------------
    /** 次优配方是否限制 总养份保持不变  */
    var isLimitResultNutrient = true

    /** 次优配方是否限制 使用原料保持不变  */
    var isLimitResultMaterials = true

    /** 是否允许烘干  */
    var isAllowDrying = true

    /** 工厂  */
    var factory: String? = null

    /** 原料  */
    var materials: LinkedHashMap<String, Material>

    /** 限用原料  */
    private var limitUseMaterialNames: MutableList<String>? = null

    /** 不使用的原料  */
    var notUseMaterialNames: MutableList<String>? = null

    /** 成品成份目标,总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 水分 硼 锌 成份限用原料  */
    lateinit var componentTarget: Components

    /** 不能混用的原料,value: 原料名称片段  */
    var notMixMaterials: MutableList<Array<String>>? = null

    /** 原料要求,key:原料名称片段, value: 原料使用限制  */
    var materialReq: MutableMap<String, Limit>? = null

    /** 条件限制，当条件1满足时，条件2必须满足  */
    var conditions: MutableMap<Condition, Condition>? = null

    /** 原料之间的用量关系，key: 原料名称片段, value: 其他原料与key的系数关系  */
    var materialRelations: MutableMap<String, MutableMap<String, MaterialRatio>>? = null

    /** 是否限制液氨系数  */
    var isLimitLiquidAmmonia = false

    /** 是否限制硫酸系数  */
    var isLimitVitriol = false

    /** 原料价格,key: 原料名称, value: 原料分工厂价格  */
    private val materialPrices: Map<String, MaterialPrice>

    /** 原始原料名称  */
    private val originalMaterialNames: MutableList<String> = ArrayList()

    private val materialComparator = kotlin.Comparator<Material> { o1, o2 ->
        if (o1.price == o2.price)
            originalMaterialNames.indexOf(o1.name).compareTo(originalMaterialNames.indexOf(o2.name))
        else
            o1.price!!.compareTo(o2.price!!)
    }

    // --------------------------------------------
    init {
        // 读取配方要求
        val specialPrice = readRecipe()

        // 读取原料价格
        materialPrices = readMaterialPrices(specialPrice)
        materials = LinkedHashMap()
        // 读取原料
        val materialMap = readMaterials()
        val materialPredicate = Predicate { m: Material ->
            if (m.price == null) {
                return@Predicate false
            }
            val materialName = m.name
            // 过滤不使用的原料
            if (notUseMaterialNames!!.contains(materialName)) {
                return@Predicate false
            }

            // 有用量限制的原料
            for (materialNameFragment in materialReq!!.keys) {
                val limit = materialReq!![materialNameFragment]!!
                val min = limit.min
                if (min != null && min > 0.0) {
                    if (materialName!!.contains(materialNameFragment)) {
                        return@Predicate true
                    }
                }
            }

            // 过滤不在成份限制的原料
            for (index in componentTarget.keys) {
                val limit = componentTarget[index]!!
                if (limit.materials != null) {
                    val value = m.components?.get(index)?.value
                    if (value != null && value > 0.0) {
                        return@Predicate limit.materials!!.contains(materialName)
                    }
                }
            }

            // 限用的原料
            if (limitUseMaterialNames!!.isNotEmpty()) {
                if (!limitUseMaterialNames!!.contains(materialName)) return@Predicate false
            }

            // 有用量限制的原料
            for (materialNameFragment in materialReq!!.keys) {
                if (materialName!!.contains(materialNameFragment)) {
                    val limit = materialReq!![materialNameFragment]
                    if (limit != null) {
                        val limitMaterials = limit.materials
                        if (limitMaterials != null) {
                            return@Predicate limitMaterials.contains(materialName)
                        }
                    }
                }
            }

            // 条件限制的物料不过滤
            for (condition in conditions!!.values) {
                val materialNameFragment = condition.materialNameFragment
                if (materialName!!.contains(materialNameFragment)) {
                    return@Predicate true
                }
            }

            // 包含限制用量关系的原料
            if (materialRelations!!.isNotEmpty()) {
                val materialNames: Set<String?> = materialRelations!!.keys
                for (name in materialNames) {
                    if (materialName!!.contains(name!!)) {
                        return@Predicate true
                    }
                }
            }
            true
        }
        val materialCollection: Collection<Material> = materialMap.values.stream().filter(materialPredicate).collect(Collectors.toList())

        // 必要的
        if (limitUseMaterialNames!!.isNotEmpty()) {
            materials.putAll(
                    materialCollection.stream()
                            .filter { m: Material -> limitUseMaterialNames!!.contains(m.name) }
                            .collect(Collectors.toMap({ obj: Material -> obj.name!! }, { m: Material -> m })))
        } else {
            // 按Fragment分组
            val others: MutableSet<Material> = HashSet(materialCollection)
            val fragments = materialNameFragments()
            for (fragment in fragments) {
                val collect = materialCollection.stream()
                        .filter { m: Material -> m.name!!.contains(fragment) }
                        .collect(Collectors.toList())
                collect.stream()
                        .collect(Collectors.groupingBy { m: Material -> m.components!!.key })
                        .values
                        .forEach(
                                Consumer { list: List<Material> ->
                                    if (list.size > 1) {
                                        val material = list.sortedWith(materialComparator)[0]
                                        materials[material.name!!] = material
                                    } else {
                                        val material = list[0]
                                        materials[material.name!!] = material
                                    }
                                })
                collect.forEach(Consumer { o: Material -> others.remove(o) })
            }

            // 筛选价格低的其他原料
            others.stream()
                    .collect(Collectors.groupingBy { m: Material -> m.components!!.key })
                    .values
                    .forEach(
                            Consumer { list: List<Material> ->
                                if (list.size > 1) {
                                    val material = list.sortedWith(materialComparator)[0]
                                    materials[material.name!!] = material
                                } else {
                                    val material = list[0]
                                    materials[material.name!!] = material
                                }
                            })
        }

        // 物料排序
        val map = LinkedHashMap<String, Material>()
        for (m in materials.keys.sortedBy
        { originalMaterialNames.indexOf(it) }) {
            map[m] = materials[m]!!
        }
        materials = map
    }

    private fun materialNameFragments(): Set<String> {
        val materialNameFragments: MutableSet<String> = HashSet()
        // 包含限制用量关系的原料
        if (materialRelations!!.isNotEmpty()) {
            val materialNames: Set<String> = materialRelations!!.keys
            materialNameFragments.addAll(materialNames)
        }

        // 条件限制的物料不过滤
        for (condition in conditions!!.values) {
            val materialNameFragment = condition.materialNameFragment
            materialNameFragments.add(materialNameFragment)
        }

        // 有用量限制的原料
        for (materialNameFragment in materialReq!!.keys) {
            val limit = materialReq!![materialNameFragment]
            val min = limit?.min
            if (min != null && min > 0.0) {
                materialNameFragments.add(materialNameFragment)
            }
        }
        return materialNameFragments
    }

    private fun readRecipe(): Map<String, Long> {
        // 不能混用的原料 不使用的原料 原料限制 限用原料 成份原料限制 项目  总养分 氮含量 磷含量 水溶磷率 钾含量 氯离子 产品水分 硼 锌 防结粉用量（公斤/吨产品）
        // 防结油用量（公斤/吨产肥 ） 喷浆专用尿素用量（公斤/吨） 磷酸耗液氨系数 硫酸耗液氨系数 再浆耗液氨系数 磷铵耗液氮系数
        val workbook = ReadableWorkbook(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream)
        val sheet = workbook
                .findSheet(productName)
                .orElseThrow { RuntimeException("找不到配方$productName") }
        val rows = sheet.openStream().collect(Collectors.toMap({ obj: Row -> obj.rowNum }, { r: Row -> r }))
        // 工厂
        factory = rows[1]!!.getCellAsString(4).orElseThrow { RuntimeException("找不到工厂") }

        // 附加条件起始行
        val conditionStartRow = 13
        // 附加条件起始列
        var conditionStartCol = 0
        // 特殊价格
        val specialPriceNameCol = 0
        conditionStartCol++
        val specialPriceCol = conditionStartCol
        conditionStartCol++
        val specialPrice: MutableMap<String, Long> = HashMap()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    val materialName = row.getCellAsString(specialPriceNameCol).orElse(null)
                    if (StringUtils.hasText(materialName)) {
                        row.getCellAsNumber(specialPriceCol)
                                .ifPresent { price: BigDecimal -> specialPrice[materialName] = price.toLong() }
                    }
                }
        // 不能混用的原料
        val notMixMaterialCol = conditionStartCol
        conditionStartCol++
        notMixMaterials = mutableListOf()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(notMixMaterialCol)
                            .ifPresent { str: String ->
                                if (StringUtils.hasText(str)) {
                                    val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    notMixMaterials!!.add(split)
                                }
                            }
                }

        // 不使用的原料
        val notUseMaterialCol = conditionStartCol
        conditionStartCol++
        notUseMaterialNames = mutableListOf()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(notUseMaterialCol)
                            .ifPresent { str: String? ->
                                if (!str.isNullOrBlank()) {
                                    notUseMaterialNames!!.add(str)
                                }
                            }
                }

        // 原料限制
        val materialReqCol = conditionStartCol
        conditionStartCol++
        materialReq = mutableMapOf()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(materialReqCol)
                            .ifPresent { str: String ->
                                if (StringUtils.hasText(str)) {
                                    val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    val key = split[0]
                                    val limit = materialReq!!.computeIfAbsent(key) { _: String? -> Limit() }
                                    limit.materials = (
                                            Arrays.stream(Arrays.copyOfRange(split, 1, split.size))
                                                    .collect(Collectors.toList()))
                                }
                            }
                }

        // 限用原料
        val limitUseMaterialCol = conditionStartCol
        conditionStartCol++
        limitUseMaterialNames = mutableListOf()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(limitUseMaterialCol)
                            .ifPresent { str: String? ->
                                if (!str.isNullOrBlank()) {
                                    limitUseMaterialNames!!.add(str)
                                }
                            }
                }

        // 条件限制
        // 跳过成份原料限制
        conditionStartCol++
        val conditionCol = conditionStartCol
        //    conditionStartCol++;
        conditions = mutableMapOf()
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(conditionCol)
                            .ifPresent { str: String ->
                                if (StringUtils.hasText(str)) {
                                    val split = str.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    if (split.size == 2) {
                                        val condition1 = Condition(split[0])
                                        val condition2 = Condition(split[1])
                                        conditions!![condition1] = condition2
                                    }
                                }
                            }
                }

        // 成品成份要求
        componentTarget = Components()
        var index = 2
        val targetMaxLimitRow = rows[10]
        val targetMinLimitRow = rows[11]
        // 总养份
        var min = targetMinLimitRow!!.getCell(index).value as BigDecimal
        var max = targetMaxLimitRow!!.getCell(index++).value as BigDecimal
        componentTarget.totalNutrient = (Limit(min, max))
        // 氮含量 磷含量 水溶磷率 钾含量 氯离子 产品水分 硼 锌
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.nitrogen = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.phosphorus = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.waterSolublePhosphorusRate = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.potassium = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.chlorine = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.water = (Limit(min, max))
        // 跳过系统物料水分
        index++
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.boron = (Limit(min, max))
        min = targetMinLimitRow.getCell(index).value as BigDecimal
        max = targetMaxLimitRow.getCell(index++).value as BigDecimal
        componentTarget.zinc = (Limit(min, max))
        // 成份原料限制
        rows.values.stream()
                .filter { row: Row -> row.rowNum > conditionStartRow }
                .forEach { row: Row ->
                    row.getCellAsString(6)
                            .ifPresent { str: String ->
                                if (StringUtils.hasText(str)) {
                                    val split = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    val limitMaterials = Arrays.stream(Arrays.copyOfRange(split, 1, split.size))
                                            .collect(Collectors.toList())
                                    val name = split[0]
                                    val limit = componentTarget.getLimit(name)
                                    limit!!.materials = limitMaterials
                                }
                            }
                }
        val limitRowStart = 4
        val maxLimitCol = 7
        val minLimitCol = 8
        // 原料使用限制
        for (i in 0..2) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (StringUtils.hasText(materialNameFragment)) {
                materialNameFragment = materialNameFragment
                        .replace("用量（公斤/吨）", "")
                        .replace("用量（公斤/吨产肥 ）", "")
                        .replace("用量（公斤/吨产肥）", "")
                        .replace("用量（公斤/吨产品）", "")
                val maxUse = rows[maxLimitCol]!!
                        .getCellAsNumber(index)
                        .orElseThrow { RuntimeException("找不到用量") }
                val minUse = rows[minLimitCol]!!
                        .getCellAsNumber(index)
                        .orElseThrow { RuntimeException("找不到用量") }
                val limit = materialReq!!.computeIfAbsent(materialNameFragment) { _: String? -> Limit() }
                limit.min = minUse.toDouble()
                limit.max = maxUse.toDouble()
            }
            index++
        }
        // 原料之间的用量关系
        materialRelations = mutableMapOf()
        // 液氨
        for (i in 0..4) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (StringUtils.hasText(materialNameFragment)) {
                isLimitLiquidAmmonia = true
                val isexcess = materialNameFragment.contains("过量")
                if (isexcess) {
                    materialNameFragment = materialNameFragment.replace("过量", "")
                }
                materialNameFragment = materialNameFragment
                        .replace("耗液氨系数", "")
                        .replace("耗液氮系数", "")
                        .replace("氯化钾反应需(.*)量".toRegex(), "$1")
                        .replace("氯化钾反应所需(.*)量".toRegex(), "$1")
                val maxUse = rows[maxLimitCol]!!.getCell(index).value as BigDecimal
                val minUse = rows[minLimitCol]!!.getCell(index).value as BigDecimal
                val materialRatioMap = materialRelations!!.computeIfAbsent(liquidAmmonia) { _: String? -> HashMap() }
                val materialRatio = materialRatioMap.computeIfAbsent(materialNameFragment) { _: String? -> MaterialRatio() }
                val limit = Limit(minUse, maxUse)
                if (isexcess) {
                    materialRatio.originExcess = limit
                } else {
                    materialRatio.normal = limit
                }
            }
            index++
        }
        // 硫酸
        for (i in 0..1) {
            var materialNameFragment = rows[limitRowStart]!!.getCellAsString(index).orElse(null)
            if (StringUtils.hasText(materialNameFragment)) {
                isLimitVitriol = true
                val isexcess = materialNameFragment.contains("过量")
                if (isexcess) {
                    materialNameFragment = materialNameFragment.replace("过量", "")
                }
                materialNameFragment = materialNameFragment.replace("反应所需硫酸系数", "").replace("反应需硫酸量系数", "")
                val maxUse = rows[maxLimitCol]!!.getCell(index).value as BigDecimal
                val minUse = rows[minLimitCol]!!.getCell(index).value as BigDecimal
                val materialRatioMap = materialRelations!!.computeIfAbsent(vitriol) { _: String? -> HashMap() }
                val materialRatio = materialRatioMap.computeIfAbsent(materialNameFragment) { _: String? -> MaterialRatio() }
                val limit = Limit(minUse, maxUse)
                if (isexcess) {
                    materialRatio.excess = limit
                } else {
                    materialRatio.normal = limit
                }
            }
            index++
        }
        return specialPrice
    }

    // --------------------------------------------
    val fileName: String
        /** 输出文件名  */
        get() = ((productName
                + if (numMaxMaterials <= 0) "配方计算结果-进料口不限" else "配方计算结果-进料口不大于$numMaxMaterials")
                + if (isAllowDrying) "-允许烘干" else "-不允许烘干")

    /** 获取原料成份,key: 原料名称 value: 原料成份  */
    private fun readMaterials(): Map<String?, Material> {
        // 读取原料成份：序号 大类 原料名称 原料形态 氮含量 磷含量 钾含量 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅
        // 指标23 指标24 指标25 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40
        // 指标41 指标42 指标43 指标44 指标45 指标46 指标47 指标48 指标49 指标50
        val excelFields: Array<ExcelField<ComponentsForm, *>> =
                arrayOf(
                        ExcelField.of("大类", ComponentsForm::category),
                        ExcelField.of("原料名称", ComponentsForm::name),
                        ExcelField.of("原料形态", ComponentsForm::form),
                        ExcelField.of("氮含量", ComponentsForm::nitrogen).defaultValue(0.0),
                        ExcelField.of("磷含量", ComponentsForm::phosphorus).defaultValue(0.0),
                        ExcelField.of("钾含量", ComponentsForm::potassium).defaultValue(0.0),
                        ExcelField.of("氯离子", ComponentsForm::chlorine).defaultValue(0.0),
                        ExcelField.of("水分", ComponentsForm::water).defaultValue(0.0),
                        ExcelField.of("水溶磷率", ComponentsForm::waterSolublePhosphorusRate)
                                .defaultValue(0.0),
                        ExcelField.of("水溶磷", ComponentsForm::waterSolublePhosphorus)
                                .defaultValue(0.0),
                        ExcelField.of("硝态氮", ComponentsForm::nitrateNitrogen).defaultValue(0.0),
                        ExcelField.of("硼", ComponentsForm::boron).defaultValue(0.0),
                        ExcelField.of("锌", ComponentsForm::zinc).defaultValue(0.0),
                        ExcelField.of("锰", ComponentsForm::manganese).defaultValue(0.0),
                        ExcelField.of("铜", ComponentsForm::copper).defaultValue(0.0),
                        ExcelField.of("铁", ComponentsForm::iron).defaultValue(0.0),
                        ExcelField.of("钼", ComponentsForm::molybdenum).defaultValue(0.0),
                        ExcelField.of("镁", ComponentsForm::magnesium).defaultValue(0.0),
                        ExcelField.of("硫", ComponentsForm::sulfur).defaultValue(0.0),
                        ExcelField.of("钙", ComponentsForm::calcium).defaultValue(0.0),
                        ExcelField.of("有机质（%）", ComponentsForm::organicMatter).defaultValue(0.0),
                        ExcelField.of("腐植酸", ComponentsForm::humicAcid).defaultValue(0.0),
                        ExcelField.of("黄腐酸", ComponentsForm::fulvicAcid).defaultValue(0.0),
                        ExcelField.of("活性菌", ComponentsForm::activeBacteria).defaultValue(0.0),
                        ExcelField.of("硅", ComponentsForm::silicon).defaultValue(0.0),
                        ExcelField.of("指标23", ComponentsForm::index23).defaultValue(0.0),
                        ExcelField.of("指标24", ComponentsForm::index24).defaultValue(0.0),
                        ExcelField.of("指标25", ComponentsForm::index25).defaultValue(0.0),
                        ExcelField.of("指标26", ComponentsForm::index26).defaultValue(0.0),
                        ExcelField.of("指标27", ComponentsForm::index27).defaultValue(0.0),
                        ExcelField.of("指标28", ComponentsForm::index28).defaultValue(0.0),
                        ExcelField.of("指标29", ComponentsForm::index29).defaultValue(0.0),
                        ExcelField.of("指标30", ComponentsForm::index30).defaultValue(0.0),
                        ExcelField.of("指标31", ComponentsForm::index31).defaultValue(0.0),
                        ExcelField.of("指标32", ComponentsForm::index32).defaultValue(0.0),
                        ExcelField.of("指标33", ComponentsForm::index33).defaultValue(0.0),
                        ExcelField.of("指标34", ComponentsForm::index34).defaultValue(0.0),
                        ExcelField.of("指标35", ComponentsForm::index35).defaultValue(0.0),
                        ExcelField.of("指标36", ComponentsForm::index36).defaultValue(0.0),
                        ExcelField.of("指标37", ComponentsForm::index37).defaultValue(0.0),
                        ExcelField.of("指标38", ComponentsForm::index38).defaultValue(0.0),
                        ExcelField.of("指标39", ComponentsForm::index39).defaultValue(0.0),
                        ExcelField.of("指标40", ComponentsForm::index40).defaultValue(0.0),
                        ExcelField.of("指标41", ComponentsForm::index41).defaultValue(0.0),
                        ExcelField.of("指标42", ComponentsForm::index42).defaultValue(0.0),
                        ExcelField.of("指标43", ComponentsForm::index43).defaultValue(0.0),
                        ExcelField.of("指标44", ComponentsForm::index44).defaultValue(0.0),
                        ExcelField.of("指标45", ComponentsForm::index45).defaultValue(0.0),
                        ExcelField.of("指标46", ComponentsForm::index46).defaultValue(0.0),
                        ExcelField.of("指标47", ComponentsForm::index47).defaultValue(0.0),
                        ExcelField.of("指标48", ComponentsForm::index48).defaultValue(0.0),
                        ExcelField.of("指标49", ComponentsForm::index49).defaultValue(0.0),
                        ExcelField.of("指标50", ComponentsForm::index50).defaultValue(0.0))

        // 读取 Excel
        var componentsForms = ExcelImport.of(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream)
                .sheet("原料成分表")
                .setColumn(1)
                .getData<ComponentsForm, ComponentsForm>(excelFields)

        // 转换为Map
        componentsForms = componentsForms.stream()
                .filter { c: ComponentsForm -> StringUtils.hasText(c.name) }
                .collect(Collectors.toList())
        val materials: MutableMap<String?, Material> = HashMap()
        for (component in componentsForms) {
            val material = Material()
            val materialName = component.name!!
            originalMaterialNames.add(materialName)
            materials[materialName] = material
            material.name = materialName
            material.category = component.category
            material.form = component.form
            val materialPrice = materialPrices[materialName]
            if (materialPrice != null) {
                material.price = materialPrice.getPrice(factory!!)
            }
            val components = Components()
            material.components = components
            // 设置所有的指标
            components.totalNutrient = Limit(component.totalNutrient)
            components.nitrogen = Limit(component.nitrogen)
            components.phosphorus = Limit(component.phosphorus)
            components.potassium = Limit(component.potassium)
            components.chlorine = Limit(component.chlorine)
            components.water = Limit(component.water)
            components.waterSolublePhosphorusRate = Limit(component.waterSolublePhosphorusRate)
            components.waterSolublePhosphorus = Limit(component.waterSolublePhosphorus)
            components.nitrateNitrogen = Limit(component.nitrateNitrogen)
            components.boron = Limit(component.boron)
            components.zinc = Limit(component.zinc)
            components.manganese = Limit(component.manganese)
            components.copper = Limit(component.copper)
            components.iron = Limit(component.iron)
            components.molybdenum = Limit(component.molybdenum)
            components.magnesium = Limit(component.magnesium)
            components.sulfur = Limit(component.sulfur)
            components.calcium = Limit(component.calcium)
            components.organicMatter = Limit(component.organicMatter)
            components.humicAcid = Limit(component.humicAcid)
            components.fulvicAcid = Limit(component.fulvicAcid)
            components.activeBacteria = Limit(component.activeBacteria)
            components.silicon = Limit(component.silicon)
            // 设置指标23-50
            components.index23 = Limit(component.index23)
            components.setIndex24(Limit(component.index24))
            components.setIndex25(Limit(component.index25))
            components.setIndex26(Limit(component.index26))
            components.setIndex27(Limit(component.index27))
            components.setIndex28(Limit(component.index28))
            components.setIndex29(Limit(component.index29))
            components.setIndex30(Limit(component.index30))
            components.setIndex31(Limit(component.index31))
            components.setIndex32(Limit(component.index32))
            components.setIndex33(Limit(component.index33))
            components.index34 = Limit(component.index34)
            components.index35 = Limit(component.index35)
            components.index36 = Limit(component.index36)
            components.index37 = Limit(component.index37)
            components.index38 = Limit(component.index38)
            components.index39 = Limit(component.index39)
            components.index40 = Limit(component.index40)
            components.index41 = Limit(component.index41)
            components.index42 = Limit(component.index42)
            components.index43 = Limit(component.index43)
            components.index44 = Limit(component.index44)
            components.index45 = Limit(component.index45)
            components.index46 = Limit(component.index46)
            components.index47 = Limit(component.index47)
            components.index48 = Limit(component.index48)
            components.index49 = Limit(component.index49)
            components.index50 = Limit(component.index50)
        }
        return materials
    }

    /** 获取原料价格,key: 原料名称 value: 原料价格  */
    private fun readMaterialPrices(specialPrices: Map<String, Long>): Map<String, MaterialPrice> {
        // 读取原料价格:原料名称 荆州 宜城 应城 宁陵 平原 眉山 新疆 铁岭 肇东 佳木斯
        val excelFields: Array<ExcelField<MaterialPriceForm, *>> = arrayOf(
                ExcelField.of("原料名称", MaterialPriceForm::name),
                ExcelField.of("报价日期", MaterialPriceForm::name).setter { _: MaterialPriceForm, _: String? -> },
                ExcelField.of("荆州", MaterialPriceForm::jingzhou),
                ExcelField.of("宜城", MaterialPriceForm::yicheng),
                ExcelField.of("应城", MaterialPriceForm::yingcheng),
                ExcelField.of("宁陵", MaterialPriceForm::ningling),
                ExcelField.of("平原", MaterialPriceForm::pingyuan),
                ExcelField.of("眉山", MaterialPriceForm::meishan),
                ExcelField.of("新疆", MaterialPriceForm::xinjiang),
                ExcelField.of("铁岭", MaterialPriceForm::tieling),
                ExcelField.of("肇东", MaterialPriceForm::zhaodong),
                ExcelField.of("佳木斯", MaterialPriceForm::jiamusi))

        // 读取 Excel
        val materialPriceForms = ExcelImport.of(ClassPathResource("配方报价管理系统需求清单.xlsx").inputStream)
                .sheet("原料价格表")
                .setColumn(2)
                .getData<MaterialPriceForm, MaterialPriceForm>(excelFields)

        // 转换为Map  获取原料价格 key: 原料名称 value: 原料价格
        val materialPriceMap: MutableMap<String, MaterialPrice> = materialPriceForms.stream()
                .filter { p: MaterialPriceForm -> StringUtils.hasText(p.name) }
                .collect(
                        Collectors.toMap(MaterialPriceForm::name
                        ) { p: MaterialPriceForm ->
                            val materialPrice = MaterialPrice()
                            materialPrice["荆州"] = p.jingzhou
                            materialPrice["宜城"] = p.yicheng
                            materialPrice["应城"] = p.yingcheng
                            materialPrice["宁陵"] = p.ningling
                            materialPrice["平原"] = p.pingyuan
                            materialPrice["眉山"] = p.meishan
                            materialPrice["新疆"] = p.xinjiang
                            materialPrice["铁岭"] = p.tieling
                            materialPrice["肇东"] = p.zhaodong
                            materialPrice["佳木斯"] = p.jiamusi
                            materialPrice
                        })
        // 特殊价格设置
        specialPrices.forEach { (name: String, price: Long) ->
            val materialPrice = materialPriceMap.computeIfAbsent(name) { _: String -> MaterialPrice() }
            materialPrice[factory!!] = price
        }
        return materialPriceMap
    }

    @get:Throws(Exception::class)
    val costMap: Map<String, Long>
        // --------------------------------------------
        get() {
            val costLines = readLines("/$productName/原料价格.txt")
            val costMap: MutableMap<String, Long> = HashMap()
            for (i in 1 until costLines.size) {
                val split = costLines[i].split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                costMap[split[0]] = split[1].toLong()
            }
            return costMap
        }

    fun readLines(fileName: String): List<String> {
        val stream = ReqData::class.java.getResourceAsStream(fileName)
                ?: return emptyList()
        return FileUtil.readLines(stream, StandardCharsets.UTF_8).stream().filter { str: String? -> StringUtils.hasText(str) }.map { obj: String -> obj.trim { it <= ' ' } }.collect(Collectors.toList())
    }

    // 检查价格
    fun checkPrice() {
        val costMap = costMap
        for (name in costMap.keys) {
            // 价格存在
            Assert.isTrue(materialPrices.containsKey(name), name + "价格不存在")
            val price = costMap[name]
            val actPrice = materialPrices[name]!![factory]
            Assert.isTrue(
                    actPrice != null && actPrice == price,
                    name + " 价格" + price + "!=" + actPrice + "不一致")
        }
    }

    companion object {
        /** 碳铵原料名称  */
        const val cliquidAmmonia = "碳铵"

        /** 液氨原料名称  */
        const val liquidAmmonia = "液氨"

        /** 硫酸原料名称  */
        const val vitriol = "硫酸"

        /** 液氨 对应 碳铵 使用量比例  */
        val la2CAUseRatio = 4.7647
        fun isNeedLiquidAmmon(materialNameFragment: String?, materialName: String?): Boolean {
            val needLiquidAmmon: Boolean = when (materialNameFragment) {
                "硫酸" -> {
                    materialName == materialNameFragment
                }

                "磷酸" -> {
                    materialName == materialNameFragment
                }

                else -> {
                    materialName!!.contains(materialNameFragment!!)
                }
            }
            return needLiquidAmmon
        }
    }
}
