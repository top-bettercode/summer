package top.bettercode.summer.test.web.support.setting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.property.PropertiesSource

/**
 * @author Peter Wu
 */
internal class PropertiesSourceTest {
    @Test
    fun mapOf() {
        val propertiesSource = PropertiesSource("base-messages")
        val typeMismatch = propertiesSource.mapOf("typeMismatch")
        Assertions.assertTrue(typeMismatch.isNotEmpty())
//        typeMismatch.forEach { (k: String, v: String) -> System.err.println("$k:$v") }
    }

    @Test
    fun test() {
        val propertiesSource = PropertiesSource("default-exception-handle")
        Assertions.assertTrue(propertiesSource.all().isNotEmpty())
//        propertiesSource.all().forEach { (k: String, v: String) -> System.err.println("$k:$v") }
    }
}