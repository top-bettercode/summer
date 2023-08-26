package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.dataObject
import top.bettercode.summer.gradle.plugin.project.template.unit.form
import top.bettercode.summer.gradle.plugin.project.template.unit.mixIn
import top.bettercode.summer.gradle.plugin.project.template.unit.serializationViews

/**
 * @author Peter Wu
 */
open class DataObject(private val overwrite: Boolean = true) : ProjectGenerator() {

    override fun content() {

        //dataObject
        +clazz(dataObjectType, overwrite = overwrite) {
            dataObject(this)
        }
    }
}
