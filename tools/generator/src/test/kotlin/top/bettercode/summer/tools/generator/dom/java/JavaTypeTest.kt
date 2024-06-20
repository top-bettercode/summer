package top.bettercode.summer.tools.generator.dom.java

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.JavaType

/**
 *
 * @author Peter Wu
 */
internal class JavaTypeTest {

    @Test
    fun isArray() {
        val javaType = JavaType("int[]")
        System.err.println(javaType.isArray)
        System.err.println(javaType)
    }
}