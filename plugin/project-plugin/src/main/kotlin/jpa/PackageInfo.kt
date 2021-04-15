import cn.bestwu.generator.dom.java.JavaType
import java.io.PrintWriter

/**
 * @author Peter Wu
 */
open class PackageInfo : ModuleJavaGenerator() {
    override val type: JavaType
        get() = packageInfoType

    override fun content() {
    }

    override fun output(printWriter: PrintWriter) {
        printWriter.println("""/**
 * $remarks
 */
package ${type.packageName};""")
    }
}