import cn.bestwu.generator.dom.java.JavaType
import java.io.PrintWriter

/**
 * @author Peter Wu
 */
open class ModulePackageInfo : ModuleJavaGenerator() {
    override val type: JavaType
        get() = modulePackageInfoType

    override fun content() {
    }

    override fun output(printWriter: PrintWriter) {
        printWriter.println("""/**
 * $moduleName
 */
package ${type.packageName};""")
    }
}