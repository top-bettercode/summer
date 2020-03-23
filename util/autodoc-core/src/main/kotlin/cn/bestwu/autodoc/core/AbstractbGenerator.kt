package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.model.Field

/**
 *
 * @author Peter Wu
 */
abstract class AbstractbGenerator {

    fun Map<String, Any?>.toFields(fields: Set<Field>, operationName: String = "", expand: Boolean = false, prefix: String = "", depth: Int = 0): List<Field> {
        val results = mutableListOf<Field>()
        this.forEach { (k, v) ->
            val key = prefix + k
            val field = fields.findField(key, v?.type ?: "String", operationName)
            field.name = k
            field.depth = depth
            results.add(field)
            if (expand && field.expanded) {
                val expandValue = field.value.toMap()
                if (expandValue != null) {
                    results.addAll(expandValue.toFields(fields, operationName, expand, "$key.", depth + 1))
                }
            }
        }
        return results
    }

    fun Set<Field>.findField(name: String, type: String, operationName: String = ""): Field {
        var field = this.find { it.name == name }!!
        if (field.description.isBlank()) {
            val possibleField = this.findPossibleField(name, type, true)
            if (possibleField == null || possibleField.description.isBlank()) {
                System.err.println("[$operationName]未找到字段[$name]的描述")
            } else {
                field = possibleField
            }
        }
        return field
    }
}