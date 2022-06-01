import jpa.unit.controllerTest
import jpa.unit.form
import jpa.unit.mixIn
import jpa.unit.testService
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

        +clazz(testServiceType, overwrite = overwrite, sourceSet = SourceSet.TEST) {
            testService(this)
        }

        +clazz(formType, overwrite = overwrite) {
            form(this)
        }

        if (!isCore)
            +interfaze(mixInType, overwrite = overwrite) {
                mixIn(this)
            }
    }
}