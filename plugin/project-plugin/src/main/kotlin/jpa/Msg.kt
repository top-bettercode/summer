import org.atteo.evo.inflector.English
import top.bettercode.generator.dsl.Generator
import java.util.Properties

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
                if (it.isPrimary)
                    properties[English.plural(it.javaName)] = remark
                properties[it.columnName] = remark
            }
        }
        if (primaryKeys.size > 1) {
            properties[entityName + "Key"] = remarks + "ID"
            properties[English.plural(entityName + "Key")] = remarks + "ID"
        }
        properties.store(file.outputStream(), "国际化")

    }
}
