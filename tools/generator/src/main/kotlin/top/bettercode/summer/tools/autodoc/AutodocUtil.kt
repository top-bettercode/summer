package top.bettercode.summer.tools.autodoc

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import top.bettercode.summer.tools.lang.util.StringUtil


/**
 *
 * @author Peter Wu
 */
object AutodocUtil {
    const val REPLACE_CHAR = "丨"
    val yamlMapper = YAMLMapper()

    init {
        init(yamlMapper)
        yamlMapper.enable(YAMLGenerator.Feature.INDENT_ARRAYS)
        yamlMapper.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        yamlMapper.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    }

    private fun init(objectMapper: ObjectMapper) {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        objectMapper.registerKotlinModule()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        objectMapper.registerModule(StringUtil.timeModule(true))
    }


}
