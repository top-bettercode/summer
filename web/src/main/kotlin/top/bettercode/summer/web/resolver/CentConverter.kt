package top.bettercode.summer.web.resolver

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.util.MoneyUtil.toCent

/**
 * 原字符串(1.2)×100转长整型(120)注解
 */
class CentConverter : ConditionalGenericConverter {
    override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
        return targetType.hasAnnotation(Cent::class.java)
    }

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(String::class.java, Long::class.java))
    }

    override fun convert(`object`: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return if (!StringUtils.hasText(`object` as String)) {
            null
        } else toCent(`object`)
    }
}
