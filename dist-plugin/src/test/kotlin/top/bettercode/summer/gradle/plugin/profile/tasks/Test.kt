package top.bettercode.summer.gradle.plugin.profile.tasks

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.internal.impldep.org.yaml.snakeyaml.Yaml
import org.junit.jupiter.api.Test

/**
 * 测试
 * @author Peter Wu
 */
class Test {

    @Test
    fun os() {
        System.err.println(Os.isFamily(Os.FAMILY_WINDOWS))
        System.err.println(Os.isFamily(Os.FAMILY_UNIX))
    }

    @Test
    fun test() {
        val inputStream =
                top.bettercode.summer.gradle.plugin.profile.tasks.Test::class.java.getResourceAsStream("/application.yml")
        val map = Yaml().loadAs(inputStream, Map::class.java)
        val result = parseYml(map)
        System.err.println(map)
        System.err.println(result)
    }

    fun parseYml(
            map: Map<*, *>,
            result: MutableMap<Any, Any> = mutableMapOf(),
            prefix: String = ""
    ): MutableMap<Any, Any> {
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
