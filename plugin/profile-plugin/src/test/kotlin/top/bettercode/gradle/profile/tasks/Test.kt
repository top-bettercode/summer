package top.bettercode.gradle.profile.tasks

import org.gradle.internal.impldep.org.yaml.snakeyaml.Yaml
import org.junit.jupiter.api.Test

/**
 * 测试
 * @author Peter Wu
 */
class Test {

    @Test
    fun test() {
        val inputStream = top.bettercode.gradle.profile.tasks.Test::class.java.getResourceAsStream("/application.yml")
        val map = Yaml().loadAs(inputStream, Map::class.java)
        val result = parseYml(map)
        System.err.println(map)
        System.err.println(result)
    }

    fun parseYml(map: Map<*, *>, result: MutableMap<Any, Any> = mutableMapOf(), prefix: String = ""): MutableMap<Any, Any> {
        map.forEach { (k, u) ->
            if (u != null) {
                if (u is Map<*, *>) {
                    parseYml(u, result, "$prefix$k.")
                } else {
                    result["$prefix$k"] = u
                }
            }
        }
        return result
    }
}
