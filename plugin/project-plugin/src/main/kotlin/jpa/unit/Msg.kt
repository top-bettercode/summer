package jpa.unit

import ProjectGenerator
import org.atteo.evo.inflector.English
import top.bettercode.generator.dom.java.element.SelfOutputUnit
import java.util.*

/**
 * @author Peter Wu
 */
val msg: ProjectGenerator.(SelfOutputUnit) -> Unit = { unit ->
    unit.apply {
        val properties = Properties()
        if (!file.exists()) {
            file.createNewFile()
        }
        properties.load(file.inputStream())
        properties[entityName] = remarks
        if (primaryKeys.size == 0) {
            properties[entityName + "Entity"] = remarks
        }
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
        if (primaryKeys.size != 1) {
            properties[entityName + "Key"] = remarks + "ID"
            properties[English.plural(entityName + "Key")] = remarks + "ID"
        }
        properties.store(file.outputStream(), "国际化")
    }
}
