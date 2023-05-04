package top.bettercode.summer.test.web.serializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.bettercode.summer.test.web.DataDicBean
import top.bettercode.summer.web.serializer.annotation.JsonCode

/**
 * @author Peter Wu
 * @since 0.1.15
 */
class CodeSerializerTest {
    @BeforeEach
    fun setUp() {
    }

    @Test
    @Throws(Exception::class)
    fun serializeInt() {
        val objectMapper = ObjectMapper()
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.addMixIn(DataDicBean::class.java, DataDicBean3::class.java)
        var dicBean = DataDicBean()
        dicBean.name = "张三"
        val string = objectMapper.writeValueAsString(dicBean)
        Assertions.assertEquals("{\"name\":\"张三\"}", string)
        dicBean = DataDicBean3()
        dicBean.name = "张三"
        dicBean.intCode = 123
        val str2 = objectMapper.writeValueAsString(dicBean)
        Assertions
                .assertEquals("{\"name\":\"张三\",\"intCode\":123,\"intCodeName\":\"123\"}", str2)
    }

    @Test
    @Throws(Exception::class)
    fun serializeString() {
        val objectMapper = ObjectMapper()
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.addMixIn(DataDicBean::class.java, DataDicBeanMin::class.java)
        var dicBean = DataDicBean()
        dicBean.name = "张三"
        val string = objectMapper.writeValueAsString(dicBean)
        Assertions.assertEquals("{\"name\":\"张三\"}", string)
        dicBean = DataDicBean3()
        dicBean.name = "张三"
        dicBean.code = "123"
        val str2 = objectMapper.writeValueAsString(dicBean)
        Assertions
                .assertEquals("{\"name\":\"张三\",\"code\":\"123\",\"codeName\":\"123\"}", str2)
    }

    internal interface DataDicBeanMin {
        @get:JsonCode("abc")
        val code: String?
    }

    internal class DataDicBean3 : DataDicBean2() {
        @get:JsonCode("abc")
        override var intCode: Int?
            get() = super.intCode
            set(intCode) {
                super.intCode = intCode
            }
    }

    internal open class DataDicBean2 : DataDicBean() {
        override var intCode: Int?
            get() = super.intCode
            set(intCode) {
                super.intCode = intCode
            }
    }
}