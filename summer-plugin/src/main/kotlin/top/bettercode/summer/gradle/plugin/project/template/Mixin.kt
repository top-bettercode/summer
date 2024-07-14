package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.mixIn
import top.bettercode.summer.gradle.plugin.project.template.unit.serializationViews

/**
 * @author Peter Wu
 */
open class Mixin(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {
        +interfaze(mixInType, overwrite = overwrite) {
            mixIn(this)
        }
        if (serializationViewsType != coreSerializationViewsType)
            +interfaze(serializationViewsType, overwrite = false) {
                serializationViews(this)
            }
    }
}
