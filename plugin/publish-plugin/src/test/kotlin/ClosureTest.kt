
import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.io.File

/**
 *
 * @author Peter Wu
 * @since
 */

class ClosureTest {

    @Test
    fun test() {
        val project = ProjectBuilder.builder().withProjectDir(File(ClosureTest::class.java.getResource("").path.substringBefore("/build/"))).build()
//        project.plugins.apply("java")
        project.plugins.apply("top.bettercode.kotlin-publish")

        (project.tasks.findByName("uploadArchives") as DefaultTask)
    }
}