package top.bettercode.autodoc.gen

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 *
 * @author Peter Wu
 */
class AutodocDisplayNameSetting : BeforeTestExecutionCallback {

    override fun beforeTestExecution(context: ExtensionContext) {
        if (context.parent.get().testClass.get().isAnnotationPresent(DisplayName::class.java)) {
            Autodoc.collectionName = context.parent.get().displayName
        }
        if (context.testMethod.get().isAnnotationPresent(DisplayName::class.java))
            Autodoc.name = context.displayName
    }

}