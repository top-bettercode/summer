package top.bettercode.summer.tools.autodoc.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(
    "name",
    "type",
    "description",
    "defaultVal",
    "value",
    "nullable",
    "canCover",
    "children"
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Field(
    var name: String = "",
    var type: String = "",
    var description: String = " ",
    var defaultVal: String = "",
    var value: String = "",
    var required: Boolean = false,
    /**
     * 从数据库生成字段描述时是否可覆盖
     */
    var canCover: Boolean = true,
    var partType: String = "",
    var children: LinkedHashSet<Field> = LinkedHashSet()
) : Comparable<Field> {

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Field) return false

        if (name != other.name) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    /**
     * 是否必填
     */
    val requiredDescription: String
        @JsonIgnore
        get() = if (required) "是" else "否"

    /**
     * postman 字段描述
     */
    val postmanDescription: String
        @JsonIgnore
        get() = (if (required) "必填," else "") + description


    override fun toString(): String {
        return "$name($type):$description"
    }
}