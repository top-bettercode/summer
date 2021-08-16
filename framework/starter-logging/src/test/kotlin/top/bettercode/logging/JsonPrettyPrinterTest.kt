package top.bettercode.logging

import io.micrometer.core.instrument.util.JsonUtils
import org.junit.jupiter.api.Test
import java.util.*
import java.util.stream.Collectors

/**
 * @author Peter Wu
 */
class JsonPrettyPrinterTest {
    @Test
    fun print() {
        val json = """{
  "libId":"lib00000184D5",
  "orderMaterialLibVos":[
    {
      "unit":"TO",
      "factoryId":"1090,1150",
      "qty":"3",
      "materialId":"000000000020101839"
    }
  ]
}
"""
        val unformattedJsonString = json.split("\n").joinToString("") { it.trim() }
        System.err.println(unformattedJsonString)
        val prettyPrint = JsonUtils.prettyPrint(unformattedJsonString)
        System.err.println(prettyPrint)
    }
}