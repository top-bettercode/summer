package cn.bestwu.autodoc.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*
import kotlin.collections.LinkedHashSet

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("name", "type", "description", "defaultVal", "value", "nullable", "canCover", "children")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Field(
        var name: String = "",
        var type: String = "",
        var partType: String = "",
        var description: String = "",
        var defaultVal: String = "",
        var value: String = "",
        var required: Boolean = false,
        /**
         * 从数据库生成字段描述时是否可覆盖
         */
        var canCover: Boolean = true,
        var children: LinkedHashSet<Field> = LinkedHashSet()) {

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