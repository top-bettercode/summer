package cn.bestwu.generator.dsl

import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.*
import java.io.PrintWriter

/**
 *
 * @author Peter Wu
 */
abstract class JavaGenerator : Generator() {
    protected val compilationUnits: MutableList<CompilationUnit> = mutableListOf()

    protected abstract fun content()

    open var packageName: String = ""
        get() {
            var packageName = if (field.isBlank()) noModulePackageName else field
            if (settings["no-modules"] == null)
                packageName = "$packageName.modules"
            return if (extension.userModule && module.isNotBlank()) {
                "$packageName.$module"
            } else {
                packageName
            }
        }

    open var noModulePackageName: String = ""
        get() {
            return if (field.isBlank()) (if (extension.packageName.isBlank()) "$basePackageName.$projectName" else extension.packageName) else field
        }

    override val dir: String
        get() = super.dir + "/" + packageName.replace('.', '/')

    protected open val type: JavaType
        get() = JavaType("$packageName.$name")
    protected open val domainJavaType: JavaType
        get() = JavaType("$packageName.${entityName.toLowerCase()}.$className")

    override fun output(printWriter: PrintWriter) {
        JavaElement.indent = extension.indent
        compilationUnits.clear()
        content()
        compilationUnits.forEach {
            printWriter.println(it.formattedContent)
        }
    }

    protected fun interfaze(visibility: JavaVisibility = JavaVisibility.PUBLIC, interfaze: Interface.() -> Unit) {
        val value = Interface(type)
        value.visibility = visibility
        interfaze(value)
        compilationUnits.add(value)
    }

    protected fun clazz(visibility: JavaVisibility = JavaVisibility.PUBLIC, clazz: TopLevelClass.() -> Unit) {
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