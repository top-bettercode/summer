package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.gradle.plugin.project.template.unit.appController
import top.bettercode.summer.gradle.plugin.project.template.unit.baseWebTest
import top.bettercode.summer.gradle.plugin.project.template.unit.controller
import top.bettercode.summer.gradle.plugin.project.template.unit.controllerTest
import top.bettercode.summer.tools.generator.dom.unit.SourceSet

/**
 * @author Peter Wu
 */
open class Controller(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {

        +clazz(appControllerType, overwrite = false) {
            appController(this)
        }

        +clazz(controllerType, overwrite = overwrite) {
            controller(this)
        }

        +clazz(baseWebTestType, overwrite = false, sourceSet = SourceSet.TEST) {
            baseWebTest(this)
        }

        +clazz(controllerTestType, overwrite = overwrite, sourceSet = SourceSet.TEST) {
            controllerTest(this)
        }
    }
}