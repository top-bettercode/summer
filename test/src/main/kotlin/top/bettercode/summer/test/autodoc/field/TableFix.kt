package top.bettercode.summer.test.autodoc.field

import org.atteo.evo.inflector.English
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.database.entity.Table

/**
 *
 * @author Peter Wu
 */
class TableFix(
    private val extension: GeneratorExtension,
    private val tableNames: Set<String>
) {

    private val tableFields: Map<String, Set<Field>> by lazy {
        val fields = mutableMapOf<String, Set<Field>>()
        extension.run { _, tableHolder ->
            tableHolder.tables().forEach {
                fields[it.tableName] = it.fields()
            }
        }
        fields
    }

    fun tableNames(): Set<Field> {
        return tableFields.filter { it.key in tableNames }.values.flatten().toSet()
    }

    fun others(): Set<Field> {
        return tableFields.filter { it.key !in tableNames }.values.flatten().toSet()
    }

    private fun Table.fields(): Set<Field> {
        val fields = columns.flatMapTo(mutableSetOf()) { column ->
            var type =
                if (column.containsSize) "${column.javaType.shortNameWithoutTypeArguments}(${column.columnSize}${if (column.decimalDigits > 0) ",${column.decimalDigits}" else ""})" else column.javaType.shortNameWithoutTypeArguments
            setOf(
                Field(
                    column.javaName, type, column.remarks, column.columnDef
                        ?: "", "", required = column.nullable
                ), Field(
                    column.columnName, type, column.remarks, column.columnDef
                        ?: "", "", required = column.nullable
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