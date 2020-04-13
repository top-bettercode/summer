package cn.bestwu.gradle.profile.tasks

import java.io.File
import java.net.URI
import java.net.URL
import java.util.*

/**
 * 测试
 * @author Peter Wu
 */
class Test {
    @org.junit.Test
    fun test() {
        val props = Properties()
        props.load(URL("file:///data/downloads/test.properties").openStream())
        System.err.println(props)
    }
}
