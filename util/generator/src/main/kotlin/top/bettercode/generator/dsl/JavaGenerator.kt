package top.bettercode.generator.dsl

import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.generator.dom.java.element.TopLevelEnumeration

/**
 *
 * @author Peter Wu
 */
abstract class JavaGenerator : Generator() {

    open var packageName: String = ""
        get() {
            return field.ifBlank {
                var packageName = field.ifBlank { basePackageName }
                if (settings["no-modules"] == null)
                    packageName = "$packageName.${settings["modules-name"] ?: extension.module}"
                if (extension.userModule && subModule.isNotBlank()) {
                    "$packageName.$subModule"
                } else {
                    packageName
                }
            }
        }

    open var basePackageName: String = ""
        get() {
            return field.ifBlank { (if (extension.projectPackage) "${extension.packageName}.$projectName" else extension.packageName) }
        }

    open fun getRemark(it: Column) =
        "${
            (if (it.remarks.isBlank()) "" else (if (it.isSoftDelete) it.remarks.split(Regex("[:：,， (（]"))[0] else it.remarks.replace(
                "@",
                "\\@"
            )))
        }${if (it.columnDef.isNullOrBlank() || it.isSoftDelete) "" else " 默认值：${it.columnDef}"}"

    open fun getParamRemark(it: Column): String {
        val remark = getRemark(it)
        return if (remark.isBlank()) "" else "@param ${it.javaName} $remark"
    }

    open fun getReturnRemark(it: Column): String {
        val remark = getRemark(it).replace(Regex(" ?([:;/]) ?"), " $1 ")
        return if (remark.isBlank()) "" else "@return $remark"
    }

    fun interfaze(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        interfaze: Interface.() -> Unit
    ) {
        val value = Interface(type, canCover, isResourcesFile, isTestFile)
        value.visibility = visibility
        interfaze(value)
        addUnit(value)
    }

    fun clazz(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        clazz: TopLevelClass.() -> Unit
    ) {
        val value = TopLevelClass(type, canCover, isResourcesFile, isTestFile)
        value.visibility = visibility
        clazz(value)
        addUnit(value)
    }

    fun enum(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        enum: TopLevelEnumeration.() -> Unit
    ) {
        val value = TopLevelEnumeration(type, canCover, isResourcesFile, isTestFile)
        enum(value)
        addUnit(value)
    }

}