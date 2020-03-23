
import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
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
        project.plugins.apply("cn.bestwu.kotlin-publish")

        (project.tasks.findByName("uploadArchives") as DefaultTask)
//        project.extensions.create("artifactory", ArtifactoryPluginConvention::class.java, project)
//        project.extensions.configure(ArtifactoryPluginConvention::class.java) { artifactory ->
//            with(artifactory) {
//                setContextUrl(project.findProperty("snapshotContextUrl"))
//
//
//                publish(delegateClosureOf<PublisherConfig> {
//
//                    repository(delegateClosureOf<PublisherConfig.Repository> {
//                                                setRepoKey(project.findProperty("snapshotRepoKey"))
////                        setUsername(project.findProperty("snapshotUsername"))
////                        setPassword(project.findProperty("snapshotPassword"))
////                        setMavenCompatible(true)
////                        setProperty("repoKey", project.findProperty("snapshotRepoKey"))
////                        setProperty("username", project.findProperty("snapshotUsername"))
////                        setProperty("password", project.findProperty("snapshotPassword"))
//                        setProperty("maven", true)
//                    })
//                    defaults(closureOf<Any> {
//                        setProperty("publications", arrayOf("mavenJava"))
//                        setProperty("publishArtifacts", true)
//                    })
//                })
//            }
//        }
//        println()

    }
}