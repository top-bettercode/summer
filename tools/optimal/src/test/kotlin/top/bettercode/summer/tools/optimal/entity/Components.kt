package top.bettercode.summer.tools.optimal.entity

import java.math.RoundingMode
import java.util.*
import java.util.stream.Collectors

/**
 * 原料成份: 总养分 氮 磷 钾 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25 指标26
 * 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44 指标45
 * 指标46 指标47 指标48 指标49 指标50
 */
class Components : LinkedHashMap<Int, Limit?>() {
    // --------------------------------------------
    // 设置
    /** 指标24  */
    fun setIndex24(limit: Limit?) {
        put(24, limit)
    }

    /** 指标25  */
    fun setIndex25(limit: Limit?) {
        put(25, limit)
    }

    /** 指标26  */
    fun setIndex26(limit: Limit?) {
        put(26, limit)
    }

    /** 指标27  */
    fun setIndex27(limit: Limit?) {
        put(27, limit)
    }

    /** 指标28  */
    fun setIndex28(limit: Limit?) {
        put(28, limit)
    }

    /** 指标29  */
    fun setIndex29(limit: Limit?) {
        put(29, limit)
    }

    /** 指标30  */
    fun setIndex30(limit: Limit?) {
        put(30, limit)
    }

    /** 指标31  */
    fun setIndex31(limit: Limit?) {
        put(31, limit)
    }

    /** 指标32  */
    fun setIndex32(limit: Limit?) {
        put(32, limit)
    }

    /** 指标33  */
    fun setIndex33(limit: Limit?) {
        put(33, limit)
    }

    // 获取
    var totalNutrient: Limit?
        /**
         * @return 总养分
         */
        get() = get(0)
        /** 总养分  */
        set(limit) {
            put(0, limit)
        }
    var nitrogen: Limit?
        /**
         * @return 氮
         */
        get() = get(1)
        /** 氮  */
        set(limit) {
            put(1, limit)
        }
    var phosphorus: Limit?
        /**
         * @return 磷
         */
        get() = get(2)
        /** 磷  */
        set(limit) {
            put(2, limit)
        }
    var potassium: Limit?
        /**
         * @return 钾
         */
        get() = get(3)
        /** 钾  */
        set(limit) {
            put(3, limit)
        }
    var chlorine: Limit?
        /**
         * @return 氯离子
         */
        get() = get(4)
        /** 氯离子  */
        set(limit) {
            put(4, limit)
        }
    var water: Limit
        /**
         * @return 水分
         */
        get() = get(5)!!
        /** 水分  */
        set(limit) {
            put(5, limit)
        }
    var waterSolublePhosphorusRate: Limit?
        /**
         * @return 水溶磷率
         */
        get() = get(6)!!
        /** 水溶磷率  */
        set(limit) {
            put(6, limit)
        }
    var waterSolublePhosphorus: Limit?
        /**
         * @return 水溶磷
         */
        get() = get(7)
        /** 水溶磷  */
        set(limit) {
            put(7, limit)
        }
    var nitrateNitrogen: Limit?
        /**
         * @return 硝态氮
         */
        get() = get(8)
        /** 硝态氮  */
        set(limit) {
            put(8, limit)
        }
    var boron: Limit?
        /**
         * @return 硼
         */
        get() = get(9)
        /** 硼  */
        set(limit) {
            put(9, limit)
        }
    var zinc: Limit?
        /**
         * @return 锌
         */
        get() = get(10)
        /** 锌  */
        set(limit) {
            put(10, limit)
        }
    var manganese: Limit?
        /**
         * @return 锰
         */
        get() = get(11)
        /** 锰  */
        set(limit) {
            put(11, limit)
        }
    var copper: Limit?
        /**
         * @return 铜
         */
        get() = get(12)
        /** 铜  */
        set(limit) {
            put(12, limit)
        }
    var iron: Limit?
        /**
         * @return 铁
         */
        get() = get(13)
        /** 铁  */
        set(limit) {
            put(13, limit)
        }
    var molybdenum: Limit?
        /**
         * @return 钼
         */
        get() = get(14)
        /** 钼  */
        set(limit) {
            put(14, limit)
        }
    var magnesium: Limit?
        /**
         * @return 镁
         */
        get() = get(15)
        /** 镁  */
        set(limit) {
            put(15, limit)
        }
    var sulfur: Limit?
        /**
         * @return 硫
         */
        get() = get(16)
        /** 硫  */
        set(limit) {
            put(16, limit)
        }
    var calcium: Limit?
        /**
         * @return 钙
         */
        get() = get(17)
        /** 钙  */
        set(limit) {
            put(17, limit)
        }
    var organicMatter: Limit?
        /**
         * @return 有机质（%）
         */
        get() = get(18)
        /** 有机质（%）  */
        set(limit) {
            put(18, limit)
        }
    var humicAcid: Limit?
        /**
         * @return 腐植酸
         */
        get() = get(19)
        /** 腐植酸  */
        set(limit) {
            put(19, limit)
        }
    var fulvicAcid: Limit?
        /**
         * @return 黄腐酸
         */
        get() = get(20)
        /** 黄腐酸  */
        set(limit) {
            put(20, limit)
        }
    var activeBacteria: Limit?
        /**
         * @return 活性菌
         */
        get() = get(21)
        /** 活性菌  */
        set(limit) {
            put(21, limit)
        }
    var silicon: Limit?
        /**
         * @return 硅
         */
        get() = get(22)
        /** 硅  */
        set(limit) {
            put(22, limit)
        }

    // 以下指标按照相同的模式生成
    var index23: Limit?
        /**
         * @return 指标23
         */
        get() = get(23)
        /** 指标23  */
        set(limit) {
            put(23, limit)
        }
    var index34: Limit?
        /**
         * @return 指标34
         */
        get() = get(34)
        /** 指标34  */
        set(limit) {
            put(34, limit)
        }
    var index35: Limit?
        /**
         * @return 指标35
         */
        get() = get(35)
        /** 指标35  */
        set(limit) {
            put(35, limit)
        }
    var index36: Limit?
        /**
         * @return 指标36
         */
        get() = get(36)
        /** 指标36  */
        set(limit) {
            put(36, limit)
        }
    var index37: Limit?
        /**
         * @return 指标37
         */
        get() = get(37)
        /** 指标37  */
        set(limit) {
            put(37, limit)
        }
    var index38: Limit?
        /**
         * @return 指标38
         */
        get() = get(38)
        /** 指标38  */
        set(limit) {
            put(38, limit)
        }
    var index39: Limit?
        /**
         * @return 指标39
         */
        get() = get(39)
        /** 指标39  */
        set(limit) {
            put(39, limit)
        }
    var index40: Limit?
        /**
         * @return 指标40
         */
        get() = get(40)
        /** 指标40  */
        set(limit) {
            put(40, limit)
        }
    var index41: Limit?
        /**
         * @return 指标41
         */
        get() = get(41)
        /** 指标41  */
        set(limit) {
            put(41, limit)
        }
    var index42: Limit?
        /**
         * @return 指标42
         */
        get() = get(42)
        /** 指标42  */
        set(limit) {
            put(42, limit)
        }
    var index43: Limit?
        /**
         * @return 指标43
         */
        get() = get(43)
        /** 指标43  */
        set(limit) {
            put(43, limit)
        }
    var index44: Limit?
        /**
         * @return 指标44
         */
        get() = get(44)
        /** 指标44  */
        set(limit) {
            put(44, limit)
        }
    var index45: Limit?
        /**
         * @return 指标45
         */
        get() = get(45)
        /** 指标45  */
        set(limit) {
            put(45, limit)
        }
    var index46: Limit?
        /**
         * @return 指标46
         */
        get() = get(46)
        /** 指标46  */
        set(limit) {
            put(46, limit)
        }
    var index47: Limit?
        /**
         * @return 指标47
         */
        get() = get(47)
        /** 指标47  */
        set(limit) {
            put(47, limit)
        }
    var index48: Limit?
        /**
         * @return 指标48
         */
        get() = get(48)
        /** 指标48  */
        set(limit) {
            put(48, limit)
        }
    var index49: Limit?
        /**
         * @return 指标49
         */
        get() = get(49)
        /** 指标49  */
        set(limit) {
            put(49, limit)
        }
    var index50: Limit?
        /**
         * @return 指标50
         */
        get() = get(50)
        /** 指标50  */
        set(limit) {
            put(50, limit)
        }
    // --------------------------------------------
    /**
     * @param name 指标名称
     * @return 成份
     */
    fun getLimit(name: String): Limit? {
        return get(componentNames.indexOf(name))
    }

    /**
     * @param name 指标名称
     * @param limit 成份
     */
    fun setLimit(name: String, limit: Limit?) {
        put(componentNames.indexOf(name), limit)
    }

    val key: String
        // --------------------------------------------
        get() = keys.stream()
                .filter { k: Int -> !isWater(k) }
                .map { k: Int? -> get(k)?.value?.setScale(4, RoundingMode.HALF_UP).toString() }
                .collect(Collectors.joining(","))

    companion object {
        private const val serialVersionUID = 1L
        const val componentNameString = "总养分 氮 磷 钾 氯离子 水分 水溶磷率 水溶磷 硝态氮 硼 锌 锰 铜 铁 钼 镁 硫 钙 有机质（%） 腐植酸 黄腐酸 活性菌 硅 指标23 指标24 指标25 指标26 指标27 指标28 指标29 指标30 指标31 指标32 指标33 指标34 指标35 指标36 指标37 指标38 指标39 指标40 指标41 指标42 指标43 指标44 指标45 指标46 指标47 指标48 指标49 指标50"
        val componentNames: MutableList<String> = Arrays.stream(componentNameString.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).collect(Collectors.toList())

        /** 是否是水溶磷率  */
        fun isWaterSolublePhosphorusRate(index: Int): Boolean {
            return index == 6
        }

        /** 是否是水  */
        fun isWater(index: Int): Boolean {
            return index == 5
        }
    }
}
