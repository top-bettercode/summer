package top.bettercode.summer.test.autodoc

import org.junit.jupiter.api.Test
import top.bettercode.summer.test.autodoc.AutodocHandler.Companion.prerequestExec
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.apisign.ApiSignProperties
import java.io.File

/**
 * @author Peter Wu
 */
class ConvertTest {

    @Test
    fun convert() {
        File("/data/repositories/bettercode/wintruelife/auction-api/app").walkTopDown()
            .filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.walkTopDown()
                .filter { it.isFile && it.extension == "yml" && it.length() > 0 && it.nameWithoutExtension != "collections" && it.nameWithoutExtension != "field" }
                .forEach { file ->
                    val docOperation =
                        AutodocUtil.yamlMapper.readValue(file, DocOperation::class.java)
                    val signProperties =
                            ApiSignProperties()
                    signProperties.clientSecret = "Ir6LrHh73VBz"
                    docOperation.prerequest = prerequestExec(docOperation, signProperties)
                    AutodocUtil.yamlMapper.writeValue(
                        File(
                            file.parent,
                            file.nameWithoutExtension + ".yml"
                        ), docOperation
                    )
                }
        }
    }
}
