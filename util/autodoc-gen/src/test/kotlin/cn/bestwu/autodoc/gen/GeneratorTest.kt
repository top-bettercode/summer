package cn.bestwu.autodoc.gen

import cn.bestwu.autodoc.core.Util
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.autodoc.core.operation.*
import cn.bestwu.autodoc.core.parseList
import cn.bestwu.autodoc.core.singleValueMap
import cn.bestwu.autodoc.core.toMap
import org.junit.Test
import java.io.File

/**
 * @author Peter Wu
 */
class GeneratorTest {


    @Test
    fun convert() {
        File("/data/repositories/bestwu/default/autodoc/core/src/doc").walkTopDown().filter { it.isDirectory && it.name == "doc" }.forEach { doc ->
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
                        request.uriVariablesExt = request.uriVariables.toFields(coFields).toSortedSet()
                        request.uriVariablesExt = request.uriVariablesExt.sorted().toSortedSet()

                        request.headersExt = request.headers.singleValueMap.toFields(coFields).toSortedSet()
                        request.headersExt.forEach {
                            it.required = (exist.request as OldDocOperationRequest).requiredHeaders.contains(it.name)
                        }
                        request.headersExt = request.headersExt.sorted().toSortedSet()

                        request.parametersExt = request.parameters.singleValueMap.toFields(coFields, expand = true).toSortedSet()
                        request.parametersExt.forEach {
                            it.required = (exist.request as OldDocOperationRequest).requiredParameters.contains(it.name)
                        }
                        request.parametersExt = request.parametersExt.sorted().toSortedSet()

                        request.partsExt = request.parts.toFields(coFields).toSortedSet()
                        request.partsExt = request.partsExt.sorted().toSortedSet()

                        request.contentExt = request.contentAsString.toMap()?.toFields(coFields, expand = true)?.toSortedSet()
                                ?: sortedSetOf()
                        request.contentExt = request.contentExt.sorted().toSortedSet()

                        response.headersExt = response.headers.singleValueMap.toFields(coFields).toSortedSet()
                        response.headersExt = response.headersExt.sorted().toSortedSet()

                        response.contentExt = response.contentAsString.toMap()?.toFields(coFields, expand = true)?.toSortedSet()
                                ?: sortedSetOf()
                        response.contentExt = response.contentExt.sorted().toSortedSet()

                        newVal.operationFile = it
                        newVal.save()
                    }
                }
            }
        }
    }
}