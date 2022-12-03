import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.gradle.plugin.project.template.controller
import top.bettercode.summer.gradle.plugin.project.template.controllerTest
import top.bettercode.summer.gradle.plugin.project.template.form
import top.bettercode.summer.tools.generator.dom.unit.SourceSet

/**
 * @author Peter Wu
 */
open class Controller(private val overwrite: Boolean = false) : ProjectGenerator() {

    override fun content() {

        +clazz(controllerType, overwrite = overwrite) {
            controller(this)
        }

        +clazz(controllerTestType, overwrite = overwrite, sourceSet = SourceSet.TEST) {
            controllerTest(this)
        }
        if (isCore) {
            //form
            +clazz(formType, overwrite = overwrite) {
                form(this)
            }
        }
    }
}