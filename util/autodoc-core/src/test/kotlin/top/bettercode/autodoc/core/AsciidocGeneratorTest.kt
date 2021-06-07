package top.bettercode.autodoc.core

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Peter Wu
 */
class AsciidocGeneratorTest {
    private lateinit var autodoc: AutodocExtension

    @BeforeEach
    fun setUp() {
        val file = File("src/doc")
        autodoc = AutodocExtension(apiHost = "http://10.13.3.205:8080", source = file, output = File("build/doc"))
        autodoc.projectName = "文档"
    }

    @Test
    fun genAdoc() {
        top.bettercode.autodoc.core.AsciidocGenerator.asciidoc(autodoc)
    }

    @Test
    fun genHtml() {
        top.bettercode.autodoc.core.AsciidocGenerator.asciidoc(autodoc)
        top.bettercode.autodoc.core.AsciidocGenerator.html(autodoc)
    }

    @Test
    fun genPdf() {
//        AsciidocGenerator.asciidoc(autodoc)
        top.bettercode.autodoc.core.AsciidocGenerator.pdf(autodoc)
    }

    @Test
    fun genHtmlPdf() {
        top.bettercode.autodoc.core.AsciidocGenerator.asciidoc(autodoc)
        top.bettercode.autodoc.core.AsciidocGenerator.html(autodoc)
        top.bettercode.autodoc.core.AsciidocGenerator.pdf(autodoc)
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
        top.bettercode.autodoc.core.AsciidocGenerator.asciidoc(autodoc)
        top.bettercode.autodoc.core.AsciidocGenerator.html(autodoc)
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