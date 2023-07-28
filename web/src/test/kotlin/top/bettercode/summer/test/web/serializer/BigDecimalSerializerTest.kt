package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.summer.test.web.DataDicBean
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier.modifyContent
import java.math.BigDecimal

/**
 * @author Peter Wu
 * @since 0.1.15
 */
class BigDecimalSerializerTest {
    @BeforeEach
    fun setUp() {
    }

    @Test
    fun serializeBigDecimal() {
        val objectMapper = ObjectMapper()
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        var dicBean = DataDicBean()
        dicBean.number1 = BigDecimal("1.234560")
        dicBean.number2 = BigDecimal("1.23456")
        dicBean.number22 = BigDecimal("1.23456")
        dicBean.number3 = BigDecimal("1.00000")
        dicBean.number4 = BigDecimal("1.00000")
        var string = objectMapper.writeValueAsString(dicBean)
        System.err.println(modifyContent(string))
        dicBean = DataDicBean()
        dicBean.number1 = BigDecimal("1.234560")
        dicBean.number2 = BigDecimal("1.20000")
        dicBean.number22 = BigDecimal("1.20000")
        dicBean.number3 = BigDecimal("1.010")
        dicBean.number4 = BigDecimal("1.010")
        string = objectMapper.writeValueAsString(dicBean)
        System.err.println(modifyContent(string))
    }
}