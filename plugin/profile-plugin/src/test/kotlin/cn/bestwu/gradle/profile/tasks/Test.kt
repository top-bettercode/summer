package cn.bestwu.gradle.profile.tasks

import org.junit.jupiter.api.Test
import java.net.URL
import java.util.*

/**
 * 测试
 * @author Peter Wu
 */
class Test {

    @Test
    fun test() {
        val props = Properties()
        props.load(URL("file:///data/downloads/test.properties").openStream())
        System.err.println(props)
    }
}
