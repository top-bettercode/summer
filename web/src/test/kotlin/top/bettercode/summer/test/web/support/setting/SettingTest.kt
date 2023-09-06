package top.bettercode.summer.test.web.support.setting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.BeanUtils
import top.bettercode.summer.tools.lang.util.StringUtil.json
import top.bettercode.summer.web.support.setting.Setting.Companion.of

/**
 * @author Peter Wu
 */
internal class SettingTest {
    var key = "summer.security.cors.enable"

    @Test
    fun testandset() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
        System.err.println(json(testCorsProperties, true))
        val testCorsProperties2 = TestCorsProperties()
        testCorsProperties2.isEnable = false
        System.err.println(json(testCorsProperties2, true))
        BeanUtils.copyProperties(testCorsProperties2, testCorsProperties)
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }

    @Test
    fun testandset1() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
//        System.err.println(json(corsProperties, true))
//        corsProperties.allowedOrigins = listOf("*", "orign")
        testCorsProperties.isEnable = false
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }

    @Test
    fun testandset2() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
        System.err.println(json(testCorsProperties, true))
        setting.put(key, "false")
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }

    @Test
    fun testandset3() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
        System.err.println(json(testCorsProperties, true))
        map[key] = "false"
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }

    @Test
    fun testandset4() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
        System.err.println(json(testCorsProperties, true))
        setting.put(key, "false")
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }

    @Test
    fun testandset5() {
        val map: MutableMap<String, String> = HashMap()
        map[key] = "true"
        val setting = of(map)
        val testCorsProperties = setting.bind(TestCorsProperties::class.java)
        System.err.println(json(testCorsProperties, true))
        setting.put(key, "false")
        System.err.println(json(testCorsProperties, true))
        System.err.println(map)
        Assertions.assertFalse(testCorsProperties.isEnable)
        Assertions.assertEquals("false", map[key])
        Assertions.assertEquals("false", setting[key])
    }
}