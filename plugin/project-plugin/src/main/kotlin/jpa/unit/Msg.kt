package jpa.unit

import ProjectGenerator
import org.atteo.evo.inflector.English
import top.bettercode.generator.dom.java.element.ManualUnit
import java.util.*

/**
 * @author Peter Wu
 */
val msg: ProjectGenerator.(msgProperties: Properties, ManualUnit) -> Unit = { properties, unit ->
    unit.apply {
        properties[entityName] = remarks
        properties[pathName] = remarks
        if (isFullComposite) {
            properties[entityName + "Entity"] = remarks
        } else {
            if (isCompositePrimaryKey) {
                properties[entityName + "Key"] = remarks + "ID"
                properties[English.plural(entityName + "Key")] = remarks + "ID"
            }
        }

        columns.forEach {
            if (it.remarks.isNotBlank()) {
                val remark = it.remarks.split(Regex("[;:：,， (（]"))[0]
                properties[it.javaName] = remark
                if (it.isPrimary)
                    properties[English.plural(it.javaName)] = remark
                properties[it.columnName] = remark
            }
        }
    }
}
