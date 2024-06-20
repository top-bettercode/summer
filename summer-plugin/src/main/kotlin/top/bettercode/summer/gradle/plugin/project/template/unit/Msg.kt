package top.bettercode.summer.gradle.plugin.project.template.unit

import org.atteo.evo.inflector.English
import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.InnerInterface
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.unit.PropertiesUnit
import top.bettercode.summer.tools.lang.capitalized

/**
 * @author Peter Wu
 */
val msg: ProjectGenerator.(PropertiesUnit) -> Unit = { unit ->
    unit.apply {
        if (remarks.isNotBlank() && this[entityName].isNullOrBlank()) {
            this[entityName] = remarks
            this[pathName] = remarks
            if (isFullComposite) {
                this[entityName + "Entity"] = remarks
            } else {
                if (isCompositePrimaryKey) {
                    this[entityName + "Key"] = remarks + "ID"
                    this[English.plural(entityName + "Key")] = remarks + "ID"
                }
            }
        }

        columns.forEach {
            if (it.remark.isNotBlank() && this[it.javaName].isNullOrBlank()) {
                val remark = it.remark.split(Regex("[;:：,， (（]"))[0]
                this[it.javaName] = remark
                if (it.isPrimary)
                    this[English.plural(it.javaName)] = remark
                this[it.columnName] = remark
            }
        }
    }
}

val coreSerializationViews: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        innerInterface(InnerInterface(JavaType("Get${pathName.capitalized()}List")))
        innerInterface(InnerInterface(JavaType("Get${pathName.capitalized()}Info")))
    }
}
