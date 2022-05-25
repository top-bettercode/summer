import jpa.unit.controllerTest
import jpa.unit.form
import jpa.unit.mixIn
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

        +clazz(formType, overwrite = overwrite) {
            form(this)
        }

        +interfaze(mixInType, overwrite = overwrite) {
            mixIn(this)
        }
    }
}