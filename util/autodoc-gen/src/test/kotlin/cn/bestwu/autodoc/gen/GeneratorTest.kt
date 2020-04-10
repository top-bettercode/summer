package cn.bestwu.autodoc.gen

import cn.bestwu.autodoc.core.*
import cn.bestwu.autodoc.core.model.DocCollection
import cn.bestwu.autodoc.core.model.DocCollections
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.*
import org.junit.Test
import java.io.File

/**
 * @author Peter Wu
 */
class GeneratorTest {


    @Test
    fun convert() {
        File("/data/repositories/bestwu/wintruelife/acceptance-api").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            val commonFields = File(doc, "field.yml").parseList(Field::class.java)
            doc.listFiles()?.filter { it.isDirectory }?.forEach {
                File(it, "collection").listFiles()?.filter { it.isDirectory }?.forEach {
                    val file = File(it, "field.yml")
                    val coFields = (file.parseList(Field::class.java) + commonFields).toMutableSet()
                    file.delete()
                    it.listFiles()?.filter { it.name != "field.yml" }?.forEach {
                        val exist = Util.yamlMapper.readValue(it, OldDocOperation::class.java)
                        coFields += exist.fields
                        val newVal = DocOperation(exist, exist.description, exist.prerequest, exist.testExec)
                        val request = newVal.request as DocOperationRequest
                        val response = newVal.response as DocOperationResponse
                        request.uriVariablesExt = request.uriVariables.toFields(coFields)

                        request.headersExt = request.headers.singleValueMap.toFields(coFields)
                        request.headersExt.forEach {
                            it.required = (exist.request as OldDocOperationRequest).requiredHeaders.contains(it.name)
                        }

                        request.parametersExt = request.parameters.singleValueMap.toFields(coFields, expand = true)
                        request.parametersExt.forEach {
                            it.required = (exist.request as OldDocOperationRequest).requiredParameters.contains(it.name)
                        }

                        request.partsExt = request.parts.toFields(coFields)

                        request.contentExt = request.contentAsString.toMap()?.toFields(coFields, expand = true)
                                ?: linkedSetOf()

                        response.headersExt = response.headers.singleValueMap.toFields(coFields)

                        response.contentExt = response.contentAsString.toMap()?.toFields(coFields, expand = true)
                                ?: linkedSetOf()

                        newVal.operationFile = it
                        newVal.save()
                    }
                }
            }
        }
    }


    @Test
    fun convert2() {
        File("/data/repositories/bestwu/wintruelife/acceptance-api").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
            doc.listFiles()?.filter { it.isDirectory }?.forEach {

                val file = File(it, "collections.yml")
                if (file.exists()) {
                    Util.yamlMapper.readValue(file.inputStream(), DocCollections::class.java).mapTo(linkedSetOf()) { (k, v) ->
                        DocCollection(k, LinkedHashSet(v), File(file.parentFile, "collection/${k}"))
                    }.forEach {
                        it.operations.forEach {
                            val request = it.request as DocOperationRequest
                            val response = it.response as DocOperationResponse

                            request.uriVariablesExt = request.uriVariables.toFields(request.uriVariablesExt)
                            request.headersExt = request.headers.singleValueMap.toFields(request.headersExt)

                            request.parametersExt = request.parameters.singleValueMap.toFields(request.parametersExt, expand = true)
                            request.partsExt = request.parts.toFields(request.partsExt)
                            request.contentExt = request.contentAsString.toMap()?.toFields(request.contentExt, expand = true)
                                    ?: linkedSetOf()

                            response.headersExt = response.headers.singleValueMap.toFields(response.headersExt)
                            response.contentExt = response.contentAsString.toMap()?.toFields(response.contentExt, expand = true)
                                    ?: linkedSetOf()

                            it.save()
                        }
                    }
                }
            }
        }
    }
}
