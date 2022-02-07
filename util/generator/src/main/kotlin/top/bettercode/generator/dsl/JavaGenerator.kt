package top.bettercode.generator.dsl

import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.*
import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
abstract class JavaGenerator : Generator() {
    private val compilationUnits: MutableList<CompilationUnit> = mutableListOf()

    protected abstract fun content()

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

    override val name: String
        get() = type.fullyQualifiedNameWithoutTypeParameters

    protected open val type: JavaType = JavaType(DEFAULT_NAME)

    /**
     * 主键
     */
    protected open val primaryKeyType: JavaType
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType
            } else {
                JavaType("$packageName.entity.${className}.${className}Key")
            }
        }


    /**
     * 主键
     */
    protected open val primaryKeyName: String
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaName
            } else {
                "${entityName}Key"
            }
        }


    protected open fun getRemark(it: Column) =
        "${
            (if (it.remarks.isBlank()) "" else (if (it.isSoftDelete) it.remarks.split(Regex("[:：,， (（]"))[0] else it.remarks.replace(
                "@",
                "\\@"
            )))
        }${if (it.columnDef.isNullOrBlank() || it.isSoftDelete) "" else " 默认值：${it.columnDef}"}"

    protected open fun getParamRemark(it: Column): String {
        val remark = getRemark(it)
        return if (remark.isBlank()) "" else "@param ${it.javaName} $remark"
    }

    protected open fun getReturnRemark(it: Column): String {
        val remark = getRemark(it)
        return if (remark.isBlank()) "" else "@return $remark"
    }

    override fun output(printWriter: PrintWriter) {
        JavaElement.indent = extension.indent
        compilationUnits.clear()
        content()
        compilationUnits.forEach {
            printWriter.println(it.formattedContent)
        }
    }

    protected fun interfaze(
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        interfaze: Interface.() -> Unit
    ) {
        val value = Interface(type)
        value.visibility = visibility
        interfaze(value)
        compilationUnits.add(value)
    }

    protected fun clazz(
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        clazz: TopLevelClass.() -> Unit
    ) {
        val value = TopLevelClass(type)
        value.visibility = visibility
        clazz(value)
        compilationUnits.add(value)
    }

    protected fun enum(enum: TopLevelEnumeration.() -> Unit) {
        val value = TopLevelEnumeration(type)
        enum(value)
        compilationUnits.add(value)
    }

}