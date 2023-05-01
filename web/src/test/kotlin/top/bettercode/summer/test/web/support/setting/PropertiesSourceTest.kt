package top.bettercode.summer.test.web.support.setting

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
        typeMismatch.forEach { (k: String, v: String) -> System.err.println("$k:$v") }
    }

    @Test
    fun test() {
        val propertiesSource = PropertiesSource("default-exception-handle")
        propertiesSource.all().forEach { (k: String, v: String) -> System.err.println("$k:$v") }
    }
}