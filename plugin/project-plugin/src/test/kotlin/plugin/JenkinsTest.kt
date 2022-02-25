package plugin

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
internal class JenkinsTest {
    private val jenkins =
        Jenkins("", "")

    @Test
    fun description() {
        val description = jenkins.description("运营后台接口")
        System.err.println(description)
    }

    @Test
    fun build() {
        jenkins.build("test-futures-front")
    }

    @Test
    fun buildInfo() {
        jenkins.buildInfo("test-futures-front")
    }
}