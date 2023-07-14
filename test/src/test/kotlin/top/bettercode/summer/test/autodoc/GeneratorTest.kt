package top.bettercode.summer.test.autodoc

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.test.autodoc.InitField.toFields
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.AutodocUtil.parseList
import top.bettercode.summer.tools.autodoc.AutodocUtil.singleValueMap
import top.bettercode.summer.tools.autodoc.AutodocUtil.toMap
import top.bettercode.summer.tools.autodoc.model.DocCollection
import top.bettercode.summer.tools.autodoc.model.DocCollections
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import java.io.File

/**
 * @author Peter Wu
 */
class GeneratorTest {
    val path = "/data/repositories/bettercode/wintruelife/old/delivery"

    //转换旧文档
    @Disabled
    @Test
    fun convert() {
        File(path).walkTopDown()
                .filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
                    val commonFields = File(doc, "field.yml").parseList(Field::class.java)
                    doc.listFiles()?.filter { it.isDirectory }?.forEach {
                        File(it, "collection").listFiles()?.filter { f -> f.isDirectory }?.forEach { path ->
                            val file = File(path, "field.yml")
                            //defaultVal: null -> defaultVal: ""
                            val coFields =
                                    if (file.exists()) {
                                        file.writeText(file.readText().replace("defaultVal: null", "defaultVal: \"\""))

                                        val coFields = (file.parseList(Field::class.java) + commonFields).toMutableSet()
                                        file.delete()
                                        coFields
                                    } else {
                                        mutableSetOf()
                                    }
                            path.listFiles()?.filter { f -> f.name != "field.yml" }?.forEach { f ->
                                //defaultVal: null -> defaultVal: ""
                                f.writeText(f.readText().replace("defaultVal: null", "defaultVal: \"\""))

                                val exist = AutodocUtil.yamlMapper.readValue(f, OldDocOperation::class.java)
                                coFields += exist.fields
                                val newVal =
                                        DocOperation(exist, exist.description, exist.prerequest, exist.testExec)
                                val request = newVal.request as DocOperationRequest
                                val response = newVal.response as DocOperationResponse
                                request.uriVariablesExt = request.uriVariables.toFields(coFields)

                                request.headersExt = request.headers.singleValueMap.toFields(coFields)
                                request.headersExt.forEach { ff ->
                                    ff.required =
                                            (exist.request as OldDocOperationRequest).requiredHeaders.contains(
                                                    ff.name
                                            )
                                }

                                request.parametersExt =
                                        request.parameters.singleValueMap.toFields(coFields, expand = true)
                                request.parametersExt.forEach { p ->
                                    p.required =
                                            (exist.request as OldDocOperationRequest).requiredParameters.contains(
                                                    p.name
                                            )
                                }

                                request.partsExt = request.parts.toFields(coFields)

                                request.contentExt =
                                        request.contentAsString.toMap()?.toFields(coFields, expand = true)
                                                ?: linkedSetOf()

                                response.headersExt = response.headers.singleValueMap.toFields(coFields)

                                response.contentExt =
                                        response.contentAsString.toMap()?.toFields(coFields, expand = true)
                                                ?: linkedSetOf()

                                newVal.operationFile = f
                                newVal.save()
                            }
                        }
                    }
                }
    }


    //处理描述
    @Disabled
    @Test
    fun convert2() {
        val file1 = File(path)
        file1.walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.listFiles()?.filter { it.isFile && it.name == "field.yml" }?.forEach {
                //defaultVal: null -> defaultVal: ""
                it.writeText(it.readText().replace("defaultVal: null", "defaultVal: \"\""))
            }
        }
        file1.walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.listFiles()?.filter { it.isDirectory }?.forEach {
                val file = File(it, "collections.yml")
                if (file.exists()) {
                    AutodocUtil.yamlMapper.readValue(file.inputStream(), DocCollections::class.java)
                            .mapTo(linkedSetOf()) { (k, v) ->
                                DocCollection(
                                        k,
                                        LinkedHashSet(v),
                                        File(file.parentFile, "collection/${k}")
                                )
                            }.forEach { dd ->
                                dd.operations.forEach { d ->
                                    val request = d.request as DocOperationRequest
                                    val response = d.response as DocOperationResponse

                                    request.uriVariablesExt =
                                            request.uriVariables.toFields(request.uriVariablesExt)
                                    request.headersExt =
                                            request.headers.singleValueMap.toFields(request.headersExt)

                                    request.parametersExt = request.parameters.singleValueMap.toFields(
                                            request.parametersExt,
                                            expand = true
                                    )
                                    request.partsExt = request.parts.toFields(request.partsExt)
                                    request.contentExt = request.contentAsString.toMap()
                                            ?.toFields(request.contentExt, expand = true)
                                            ?: linkedSetOf()

                                    response.headersExt =
                                            response.headers.singleValueMap.toFields(response.headersExt)
                                    response.contentExt = response.contentAsString.toMap()
                                            ?.toFields(response.contentExt, expand = true)
                                            ?: linkedSetOf()

                                    val genProperties = GenProperties()
                                    genProperties.rootSource = File(file1, "doc")
                                    genProperties.source = doc
                                    InitField.extFieldExt(genProperties, d)
                                    d.save()
                                }
                            }
                }
            }
        }
    }
}
