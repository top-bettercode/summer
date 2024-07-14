package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.form

/**
 * @author Peter Wu
 */
open class Form(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {

        //form
        +clazz(formType, overwrite = overwrite) {
            form(this)
        }
    }
}
