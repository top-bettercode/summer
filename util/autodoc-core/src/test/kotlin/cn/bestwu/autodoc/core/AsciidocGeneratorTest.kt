package cn.bestwu.autodoc.core

import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * @author Peter Wu
 */
class AsciidocGeneratorTest {
    lateinit var autodoc: AutodocExtension

    @Before
    fun setUp() {
        val file = File("src/doc")
        autodoc = AutodocExtension(apiHost = "http://10.13.3.205:8080", source = file, output = File("build/doc"))
        autodoc.projectName = "文档"
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

    @Test
    fun genPdf() {
//        AsciidocGenerator.asciidoc(autodoc)
        AsciidocGenerator.pdf(autodoc)
    }

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