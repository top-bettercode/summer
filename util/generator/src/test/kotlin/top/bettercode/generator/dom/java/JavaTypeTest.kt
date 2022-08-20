package top.bettercode.generator.dom.java

import org.junit.jupiter.api.Test

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