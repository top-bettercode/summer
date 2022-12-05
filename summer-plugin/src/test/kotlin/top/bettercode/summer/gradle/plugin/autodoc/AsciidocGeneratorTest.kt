package top.bettercode.summer.gradle.plugin.autodoc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.autodoc.AutodocExtension
import java.io.File
import java.util.*

/**
 * @author Peter Wu
 */
class AsciidocGeneratorTest {
    private lateinit var autodoc: AutodocExtension

    @BeforeEach
    fun setUp() {
        val file = File("src/doc")
        autodoc = AutodocExtension(source = file, output = File("build/doc"))
        autodoc.projectName = "文档"
    }

    @Test
    fun cp() {
        File(AutodocExtension::class.java.getResource("/static")!!.file).walkTopDown()
            .filter { it.isFile }.forEach {
                val path = it.path.replace(
                    "/data/repositories/bettercode/default/summer/summer-plugin/src/main/",
                    ""
                )
                System.err.println("AutodocExtension::class.java.getResourceAsStream(\"/$path\").copyTo(File(outputFile, \"$path\").outputStream())")
            }
    }

    @Test
    fun name() {
        File(AsciidocGeneratorTest::class.java.getResource("/static/Open+Sans.css")!!.file).readLines()
            .filter { it.contains("url(https:") }.forEach {
                println(it.replace(".*url\\((.*?)\\).*".toRegex(), "$1"))
            }
    }

    @Test
    fun genAdoc() {
        AsciidocGenerator.asciidoc(autodoc)
    }

    @Test
    fun genHtml() {
        AsciidocGenerator.asciidoc(autodoc)
        AsciidocGenerator.html(autodoc)
    }

    @Disabled
    @Test
    fun genPdf() {
        AsciidocGenerator.asciidoc(autodoc)
        AsciidocGenerator.pdf(autodoc)
    }

    @Test
    fun setDefaultDesc() {
        AsciidocGenerator.setDefaultDesc(autodoc, Properties())
    }

    @Disabled
    @Test
    fun genHtmlPdf() {
        AsciidocGenerator.asciidoc(autodoc)
        AsciidocGenerator.html(autodoc)
        AsciidocGenerator.pdf(autodoc)
    }

    @Test
    fun postman() {
//        autodoc.apiHost = "http://10.13.3.202:8080/npk"
//        autodoc.authUri = "/users/accessToken"
//        autodoc.authVariables = arrayOf("accessToken")
        PostmanGenerator.postman(autodoc)
    }

    @Test
    fun postmanAndHtml() {
        AsciidocGenerator.asciidoc(autodoc)
        AsciidocGenerator.html(autodoc)
        PostmanGenerator.postman(autodoc)
    }

    @Test
    fun rewrite() {
        autodoc.listModules { docModule, _ ->
            docModule.collections.forEach { collection ->
                collection.operations.forEach { operation ->
                    operation.save()
                }
            }
        }
    }

}