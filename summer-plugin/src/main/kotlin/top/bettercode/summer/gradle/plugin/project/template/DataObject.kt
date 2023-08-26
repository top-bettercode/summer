package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.dataObject

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
