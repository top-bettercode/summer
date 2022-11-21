package jpa.unit

import jpa.ProjectGenerator
import org.atteo.evo.inflector.English
import top.bettercode.generator.dom.unit.PropertiesUnit

/**
 * @author Peter Wu
 */
val msg: ProjectGenerator.(PropertiesUnit) -> Unit = { unit ->
    unit.apply {
        if (remarks.isNotBlank()) {
            this[entityName] = remarks
            this[pathName] = remarks
            if (isFullComposite) {
                this[entityName + "Entity"] = remarks
            } else {
                if (isCompositePrimaryKey) {
                    this[entityName + "Key"] = remarks + "ID"
                    this[English.plural(entityName + "Key")] = remarks + "ID"
                }
            }
        }

        columns.forEach {
            if (it.remark.isNotBlank()) {
                val remark = it.remark.split(Regex("[;:：,， (（]"))[0]
                this[it.javaName] = remark
                if (it.isPrimary)
                    this[English.plural(it.javaName)] = remark
                this[it.columnName] = remark
            }
        }
    }
}
