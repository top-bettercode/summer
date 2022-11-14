import jpa.unit.controllerTest
import jpa.unit.form
import top.bettercode.generator.dom.unit.SourceSet

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