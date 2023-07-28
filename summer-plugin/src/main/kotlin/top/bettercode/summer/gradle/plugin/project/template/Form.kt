package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.form
import top.bettercode.summer.gradle.plugin.project.template.unit.mixIn
import top.bettercode.summer.gradle.plugin.project.template.unit.serializationViews

/**
 * @author Peter Wu
 */
open class Form(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {

        //form
        +clazz(formType, overwrite = overwrite) {
            form(this)
        }
        if (!isCore) {
            +interfaze(mixInType, overwrite = overwrite) {
                mixIn(this)
            }
            if (serializationViewsType != coreSerializationViewsType)
                +interfaze(serializationViewsType, overwrite = false) {
                    serializationViews(this)
                }
        }
    }
}
