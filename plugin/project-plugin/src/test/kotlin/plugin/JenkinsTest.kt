package plugin

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
internal class JenkinsTest {
    private val jenkins =
        Jenkins("http://10.13.3.207:8080", "java-group:1114d4ed4c33206f9d373905abbfeca7d8")

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