package cn.bestwu.autodoc.core.model

import cn.bestwu.autodoc.core.Util
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.logging.operation.OperationRequestPart
import cn.bestwu.logging.operation.Parameters
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.io.File
import java.net.URI

@JsonPropertyOrder("name", "items")
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocCollection(
    override val name: String = "", var items: LinkedHashSet<String> = linkedSetOf(),
    /**
     * 集合目录
     */
    @JsonIgnore
    val dir: File
) : ICollection {

    private val log: Logger = LoggerFactory.getLogger(DocCollection::class.java)
    private fun operationFile(operationName: String): File = File(dir, "$operationName.yml")

    fun operation(operationName: String): DocOperation? {
        val operationFile = operationFile(operationName)
        return if (operationFile.exists()) {
            val docOperation = try {
                Util.yamlMapper.readValue(operationFile, DocOperation::class.java)
            } catch (e: Exception) {
                log.error(name + "/" + operationName + "解析失败")
                throw e
            }
            docOperation.operationFile = operationFile
            docOperation.collectionName = name
            docOperation.name = operationName
            if (docOperation.protocol.isBlank()) {
                docOperation.protocol = "HTTP/1.1"
            }
            docOperation.request.apply {
                this as DocOperationRequest
                uriVariables = uriVariablesExt.associate { Pair(it.name, it.value) }
                var uriString = restUri
                uriVariables.forEach { (t, u) -> uriString = uriString.replace("{${t}}", u) }

                uri = URI(uriString)

                headers = headersExt.associateTo(HttpHeaders()) { field ->
                    Pair(
                        field.name,
                        listOf(field.value)
                    )
                }
                parameters = parametersExt.associateTo(Parameters()) { field ->
                    Pair(
                        field.name,
                        listOf(field.value)
                    )
                }
                parts = partsExt.map { field ->
                    OperationRequestPart(
                        field.name,
                        field.partType,
                        headers,
                        field.value.toByteArray()
                    )
                }
            }

            docOperation.response.apply {
                this as DocOperationResponse
                headers = headersExt.associateTo(HttpHeaders()) { field ->
                    Pair(
                        field.name,
                        listOf(field.value)
                    )
                }
            }

            docOperation
        } else {
            null
        }
    }

    override val operations: List<DocOperation>
        @JsonIgnore
        get() = items.mapNotNull { operation(it) }

}

