package top.bettercode.summer.test.autodoc.field

import org.atteo.evo.inflector.English
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.PumlTableHolder
import top.bettercode.summer.tools.generator.database.entity.Table
import kotlin.collections.LinkedHashSet

/**
 *
 * @author Peter Wu
 */
class TableFix(
    private val extension: GeneratorExtension,
    private val tableNames: LinkedHashSet<String>
) {

    private val tableFields: Map<String, Set<Field>> by lazy {
        val fields = mutableMapOf<String, LinkedHashSet<Field>>()
        extension.run { _, tableHolder ->
            tableHolder as PumlTableHolder
            tableHolder.tables().forEach {
                fields[it.tableName] = it.fields()
                fields[it.className] = it.fields()
            }
        }
        fields
    }

    val namedFields: Set<Field> by lazy {
        val set = linkedSetOf<Field>()
        tableNames.forEach {
            tableFields[it]?.apply {
                set.addAll(this)
            }
        }
        set
    }

    val otherFields: Set<Field> by lazy {
        tableFields.filter { it.key !in tableNames }.values.flatten().toSet()
    }

    private fun Table.fields(): LinkedHashSet<Field> {
        val fields = columns.flatMapTo(linkedSetOf()) { column ->
            var type =
                if (column.containsSize) "${column.javaType.shortNameWithoutTypeArguments}(${column.columnSize}${if (column.decimalDigits > 0) ",${column.decimalDigits}" else ""})" else column.javaType.shortNameWithoutTypeArguments
            linkedSetOf(
                Field(
                    name = column.javaName,
                    type = type,
                    description = column.remarks
                ), Field(
                    name = column.columnName,
                    type = type,
                    description = column.remarks
                )
            )
        }
        fields.addAll(fields.map {
            Field(
                name = English.plural(it.name),
                description = it.description
            )
        })
        fields.add(Field(name = entityName, description = remarks))
        fields.add(Field(name = pathName, description = remarks))
        if (primaryKeys.isEmpty()) {
            fields.add(Field(name = entityName + "Entity", description = remarks))
        } else {
            if (primaryKeys.size > 1) {
                fields.add(Field(name = entityName + "Key", description = remarks + "主键"))
                fields.add(
                    Field(
                        name = English.plural(entityName + "Key"),
                        description = remarks + "主键"
                    )
                )
            }
        }
        return fields
    }

}