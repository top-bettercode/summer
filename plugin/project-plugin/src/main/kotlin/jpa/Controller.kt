import jpa.unit.controllerTest
import jpa.unit.form
import jpa.unit.mixIn
import top.bettercode.generator.dom.unit.SourceSet

/**
 * @author Peter Wu
 */
open class Controller : ProjectGenerator() {

    override fun content() {

        +clazz(controllerType) {
            controller(this)
        }

        +clazz(controllerTestType, sourceSet = SourceSet.TEST) {
            controllerTest(this)
        }

        +clazz(formType) {
            form(this)
        }

        +interfaze(mixInType) {
            mixIn(this)
        }
    }
}