package top.bettercode.autodoc.gen

import top.bettercode.autodoc.core.*
import top.bettercode.autodoc.core.model.DocCollection
import top.bettercode.autodoc.core.model.DocCollections
import top.bettercode.autodoc.core.model.Field
import top.bettercode.autodoc.core.operation.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Peter Wu
 */
class GeneratorTest {


    @Test
    fun convert() {
        File("/data/repositories/bettercode/wintruelife/acceptance-api").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            val commonFields = File(doc, "field.yml").parseList(Field::class.java)
            doc.listFiles()?.filter { it.isDirectory }?.forEach {
                File(it, "collection").listFiles()?.filter { f -> f.isDirectory }?.forEach { path ->
                    val file = File(path, "field.yml")
                    val coFields = (file.parseList(Field::class.java) + commonFields).toMutableSet()
                    file.delete()
                    path.listFiles()?.filter { f -> f.name != "field.yml" }?.forEach { f ->
                        val exist = Util.yamlMapper.readValue(f, OldDocOperation::class.java)
                        coFields += exist.fields
                        val newVal = DocOperation(exist, exist.description, exist.prerequest, exist.testExec)
                        val request = newVal.request as DocOperationRequest
                        val response = newVal.response as DocOperationResponse
                        request.uriVariablesExt = request.uriVariables.toFields(coFields)

                        request.headersExt = request.headers.singleValueMap.toFields(coFields)
                        request.headersExt.forEach { ff ->
                            ff.required = (exist.request as OldDocOperationRequest).requiredHeaders.contains(ff.name)
                        }

                        request.parametersExt = request.parameters.singleValueMap.toFields(coFields, expand = true)
                        request.parametersExt.forEach { p ->
                            p.required = (exist.request as OldDocOperationRequest).requiredParameters.contains(p.name)
                        }

                        request.partsExt = request.parts.toFields(coFields)

                        request.contentExt = request.contentAsString.toMap()?.toFields(coFields, expand = true)
                                ?: linkedSetOf()

                        response.headersExt = response.headers.singleValueMap.toFields(coFields)

                        response.contentExt = response.contentAsString.toMap()?.toFields(coFields, expand = true)
                                ?: linkedSetOf()

                        newVal.operationFile = f
                        newVal.save()
                    }
                }
            }
        }
    }


    @Test
    fun convert2() {
        val file1 = File("/data/repositories/bettercode/wintruelife/opsbot")
        file1.walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.listFiles()?.filter { it.isDirectory }?.forEach {

                val file = File(it, "collections.yml")
                if (file.exists()) {
                    Util.yamlMapper.readValue(file.inputStream(), DocCollections::class.java).mapTo(linkedSetOf()) { (k, v) ->
                        DocCollection(k, LinkedHashSet(v), File(file.parentFile, "collection/${k}"))
                    }.forEach { dd ->
                        dd.operations.forEach { d ->
                            val request = d.request as DocOperationRequest
                            val response = d.response as DocOperationResponse

                            request.uriVariablesExt = request.uriVariables.toFields(request.uriVariablesExt)
                            request.headersExt = request.headers.singleValueMap.toFields(request.headersExt)

                            request.parametersExt = request.parameters.singleValueMap.toFields(request.parametersExt, expand = true)
                            request.partsExt = request.parts.toFields(request.partsExt)
                            request.contentExt = request.contentAsString.toMap()?.toFields(request.contentExt, expand = true)
                                    ?: linkedSetOf()

                            response.headersExt = response.headers.singleValueMap.toFields(response.headersExt)
                            response.contentExt = response.contentAsString.toMap()?.toFields(response.contentExt, expand = true)
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
