package top.bettercode.summer.web.serializer.annotation

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import top.bettercode.summer.web.serializer.BigDecimalSerializer
import java.lang.annotation.Inherited
import java.math.RoundingMode

/**
 * @author Peter Wu
 * @since 0.1.15
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@Inherited
@JsonSerialize(using = BigDecimalSerializer::class)
annotation class JsonBigDecimal(
        /**
         * @return 分数
         */
        val divisor: String = "",
        /**
         * @return 小数位数
         */
        val scale: Int = 2, val roundingMode: RoundingMode = RoundingMode.HALF_UP,
        /**
         * @return 非科学计数法显示
         */
        val toPlainString: Boolean = false,
        /**
         * @return 是否删除尾部的0(保持数值相等)
         */
        val stripTrailingZeros: Boolean = false,
        /**
         * @return 扩展序列化百分比字段
         */
        val percent: Boolean = false)
