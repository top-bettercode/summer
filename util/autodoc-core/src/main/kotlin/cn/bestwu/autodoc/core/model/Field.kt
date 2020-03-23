package cn.bestwu.autodoc.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("name", "type", "description", "defaultVal", "value", "nullable", "expanded", "canCover")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Field(
        var name: String = "",
        var type: String = "",
        var description: String = "",
        var defaultVal: String = "",
        var value: String = "",
        var required: Boolean = false,
        @JsonIgnore
        var depth: Int = 0,
        /**
         * 是否默认展开
         */
        var expanded: Boolean = true,
        /**
         * 从数据库生成字段描述时是否可覆盖
         */
        var canCover: Boolean = true) : Comparable<Field> {

    override fun compareTo(other: Field): Int {
        return if (name == other.name && type == other.type) {
            0
        } else {
            val compareTo = description.compareTo(other.description)
            if (compareTo == 0) {
                val compareTo1 = name.compareTo(other.name)
                if (compareTo1 == 0) {
                    type.compareTo(other.type)
                } else
                    compareTo1
            } else
                compareTo
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Field) return false

        if (name != other.name) return false
        if (type != other.type) return false

        return true
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


}