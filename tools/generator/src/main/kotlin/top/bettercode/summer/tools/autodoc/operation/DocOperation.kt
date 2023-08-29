package top.bettercode.summer.tools.autodoc.operation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.lang.operation.Operation
import top.bettercode.summer.tools.lang.operation.OperationResponse
import java.io.File

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder("description", "request", "response", "protocol", "prerequest", "testExec")
@JsonIgnoreProperties("collectionName", "name", "duration")
class DocOperation(
        operation: Operation = Operation(),
        var description: String = "",
        var prerequest: List<String> = listOf(),
        var testExec: List<String> = listOf()
) : Operation(
        operation.collectionName,
        operation.name,
        operation.protocol,
        if (operation.request::class == DocOperationRequest::class) operation.request else DocOperationRequest(
                operation.request
        ),
        if (operation.response::class == DocOperationResponse::class) operation.response else DocOperationResponse(
                operation.response
        )
) {
    @JsonIgnore
    private val log = org.slf4j.LoggerFactory.getLogger(DocOperation::class.java)

    @JsonIgnore
    lateinit var operationFile: File

    fun save() {
        operationFile.parentFile.mkdirs()
        operationFile.parentFile.mkdirs()
        log.warn("${if (operationFile.exists()) "更新" else "创建"}：$operationFile")
        operationFile.writeText(AutodocUtil.yamlMapper.writeValueAsString(this))
    }

    override var request
        @JsonDeserialize(`as` = DocOperationRequest::class)
        get() = super.request
        set(value) {
            super.request = value
        }

    override var response: OperationResponse
        @JsonDeserialize(`as` = DocOperationResponse::class)
        get() = super.response
        set(value) {
            super.response = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocOperation) return false

        if (collectionName != other.collectionName) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = collectionName.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

}