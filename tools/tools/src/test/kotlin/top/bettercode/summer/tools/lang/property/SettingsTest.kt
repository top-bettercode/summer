package top.bettercode.summer.tools.lang.property

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class SettingsTest {

    @Test
    fun getDicCode() {
        Settings.dicCode.all().forEach { (k, v) ->
            System.err.println("$k:$v")
        }
    }
}