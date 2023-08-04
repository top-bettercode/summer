package top.bettercode.summer.web.resolver

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.util.StringUtils
import top.bettercode.summer.web.support.KilogramUtil.toGram

class KilogramToGramConverter : ConditionalGenericConverter {
    override fun matches(typeDescriptor: TypeDescriptor, typeDescriptor1: TypeDescriptor): Boolean {
        return typeDescriptor1.hasAnnotation(KilogramToGram::class.java)
    }

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(String::class.java, Long::class.java))
    }

    override fun convert(`object`: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (!StringUtils.hasText(`object` as String)) {
            null
        } else toGram(`object`)
    }
}
