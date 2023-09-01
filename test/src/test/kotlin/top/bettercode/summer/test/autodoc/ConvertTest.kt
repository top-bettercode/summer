package top.bettercode.summer.test.autodoc

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.apisign.ApiSignProperties
import top.bettercode.summer.test.autodoc.AutodocHandler.Companion.prerequestExec
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * @author Peter Wu
 */
class ConvertTest {
    @Test
    fun filename() {
        val fileName = "资金明细20230901232455"
        val s0 = "attachment;filename=$fileName.xlsx;filename*=UTF-8''${
            URLEncoder
                    .encode(fileName, "UTF-8")
        }.xlsx"
        val s = "attachment;filename=\"$fileName.xlsx\""
        val regex = ".*filename\\*?=+(.*?)"
        val s2 = URLDecoder .decode(s0.replace("UTF-8''", "").replace(regex.toRegex(), "$1").trim('"'), "UTF-8")
        System.err.println(s2)
        val s1 = URLDecoder .decode(s.replace("UTF-8''", "").replace(regex.toRegex(), "$1").trim('"'), "UTF-8")
        System.err.println(s1)
    }

    //处理签名
    @Disabled
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
