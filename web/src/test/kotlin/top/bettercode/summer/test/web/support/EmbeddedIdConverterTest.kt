package top.bettercode.summer.test.web.support

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil.jsonBytes
import top.bettercode.summer.web.support.EmbeddedIdConverter.toEmbeddedId
import top.bettercode.summer.web.support.EmbeddedIdConverter.toString

/**
 * @author Peter Wu
 */
class EmbeddedIdConverterTest {
    @Test
    fun testToString() {
        val embeddedIdBean = EmbeddedIdBean()
        embeddedIdBean.name = "name"
        embeddedIdBean.intCode = 1
        embeddedIdBean.price = 3L
        val s = toString(embeddedIdBean)
        System.err.println(jsonBytes(embeddedIdBean).size)
        System.err.println(s.length)
        System.err.println(s)
        val embeddedId = toEmbeddedId(s, EmbeddedIdBean::class.java)
        System.err.println(embeddedId)
        Assertions.assertEquals(embeddedIdBean, embeddedId)
    }
}
