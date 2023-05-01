package top.bettercode.summer.test.web.support.setting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.BeanUtils
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import top.bettercode.summer.web.properties.CorsProperties
import top.bettercode.summer.web.support.setting.Setting.Companion.of

/**
 * @author Peter Wu
 */
internal class SettingTest {
    var key = "summer.security.cors.enable"

    @get:Test
    val andset: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            val corsProperties2 = CorsProperties()
            corsProperties2.isEnable = false
            System.err.println(valueOf(corsProperties2, true))
            BeanUtils.copyProperties(corsProperties2, corsProperties)
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }

    @get:Test
    val andset1: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            corsProperties.allowedOrigins = listOf("*", "orign")
            corsProperties.isEnable = false
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }

    @get:Test
    val andset2: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            setting.put(key, "false")
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }

    @get:Test
    val andset3: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            map[key] = "false"
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }

    @get:Test
    val andset4: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            setting.put(key, "false")
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }

    @get:Test
    val andset5: Unit
        get() {
            val map: MutableMap<String, String> = HashMap()
            map[key] = "true"
            val setting = of(map)
            val corsProperties = setting.bind(CorsProperties::class.java)
            System.err.println(valueOf(corsProperties, true))
            setting.put(key, "false")
            System.err.println(valueOf(corsProperties, true))
            System.err.println(map)
            Assertions.assertFalse(corsProperties.isEnable)
            Assertions.assertEquals("false", map[key])
            Assertions.assertEquals("false", setting[key])
        }
}