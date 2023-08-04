package top.bettercode.summer.web.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import org.slf4j.LoggerFactory
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.util.StringUtils
import java.io.IOException

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class JsonStringConverter(private val objectMapper: ObjectMapper) : ConditionalGenericConverter {
    private val log = LoggerFactory.getLogger(JsonStringConverter::class.java)
    override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
        return true
    }

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(String::class.java, MutableCollection::class.java))
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (!StringUtils.hasText(source as String)) {
            return null
        }
        val collectionType = TypeFactory
                .defaultInstance()
                .constructCollectionType(MutableList::class.java, targetType.resolvableType.resolveGeneric(0))
        return try {
            objectMapper.readValue<Any>(source, collectionType)
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }
    }
}
