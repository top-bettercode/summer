import cn.bestwu.generator.dsl.Generator
import java.util.*

/**
 * @author Peter Wu
 */
open class Msg : Generator() {

    override val resources: Boolean
        get() = true

    override val name: String
        get() = "${if (projectName == "core") "core-" else ""}messages.properties"


    override fun doCall() {
        val properties = Properties()
        val file = destFile
        if (!file.exists()) {
            file.createNewFile()
        }
        properties.load(file.inputStream())
        properties[entityName] = remarks
        properties[pathName] = remarks
        columns.forEach {
            if (it.remarks.isNotBlank()) {
                val remark = it.remarks.split(Regex("[;:：,， (（]"))[0]
                properties[it.javaName] = remark
                properties[it.columnName] = remark
            }
        }
        properties.store(file.outputStream(), "国际化")

    }
}
