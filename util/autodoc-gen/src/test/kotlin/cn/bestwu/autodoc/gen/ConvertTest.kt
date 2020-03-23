package cn.bestwu.autodoc.gen

import cn.bestwu.api.sign.ApiSignProperties
import cn.bestwu.autodoc.core.Util
import cn.bestwu.autodoc.core.operation.DocOperation
import org.junit.Test
import java.io.File

/**
 * @author Peter Wu
 */
class ConvertTest {


    @Test
    fun convert() {
        File("/data/repositories/bestwu/wintruelife/auction-api/app").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
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
