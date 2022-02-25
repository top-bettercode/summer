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
        val description = jenkins.description("test-futures-front")
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