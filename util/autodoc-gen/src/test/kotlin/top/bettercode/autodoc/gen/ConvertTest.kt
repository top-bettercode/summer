package top.bettercode.autodoc.gen

import org.junit.jupiter.api.Test
import top.bettercode.api.sign.ApiSignProperties
import top.bettercode.autodoc.core.Util
import top.bettercode.autodoc.core.operation.DocOperation
import top.bettercode.autodoc.gen.AutodocHandler.Companion.prerequestExec
import java.io.File

/**
 * @author Peter Wu
 */
class ConvertTest {

    @Test
    fun convert() {
        File("/data/repositories/bettercode/wintruelife/auction-api/app").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.walkTopDown().filter { it.isFile && it.extension == "yml" && it.length() > 0 && it.nameWithoutExtension != "collections" && it.nameWithoutExtension != "field" }.forEach { file ->
                val docOperation = Util.yamlMapper.readValue(file, DocOperation::class.java)
                val signProperties = ApiSignProperties()
                signProperties.clientSecret = "Ir6LrHh73VBz"
                docOperation.prerequest = prerequestExec(docOperation, signProperties)
                Util.yamlMapper.writeValue(File(file.parent, file.nameWithoutExtension + ".yml"), docOperation)
            }
        }
    }
}
