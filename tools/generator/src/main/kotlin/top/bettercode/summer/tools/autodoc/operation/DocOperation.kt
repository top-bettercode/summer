package top.bettercode.summer.tools.autodoc.operation

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.http.HttpHeaders
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.operation.*
import java.io.File
import java.net.URI

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
    collectionName = operation.collectionName,
    name = operation.name,
    protocol = operation.protocol,
    request = if (operation.request::class == DocOperationRequest::class) operation.request else DocOperationRequest(
        operation.request
    ),
    response = if (operation.response::class == DocOperationResponse::class) operation.response else DocOperationResponse(
        operation.response
    )
) {
    @JsonIgnore
    lateinit var operationFile: File

    fun save() {
        operationFile.parentFile.mkdirs()
        operationFile.parentFile.mkdirs()
        if (log.isDebugEnabled)
            log.debug("${if (operationFile.exists()) "更新" else "创建"}：$operationFile")
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

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(DocOperation::class.java)

        fun DocOperation.checkBlank(appName: String?, moduleName: String) {
            val request = request as DocOperationRequest
            val response = response as DocOperationResponse

            val errors = mutableListOf<String>()
            errors.addAll(request.uriVariablesExt.checkBlank("request.uriVariablesExt"))
            errors.addAll(request.headersExt.checkBlank("request.headersExt"))
            errors.addAll(request.queriesExt.checkBlank("request.queriesExt"))
            errors.addAll(request.parametersExt.checkBlank("request.parametersExt"))
            errors.addAll(request.partsExt.checkBlank("request.partsExt"))
            errors.addAll(request.contentExt.checkBlank("request.contentExt"))

//            errors.addAll(response.headersExt.checkBlank("response.headersExt"))
            errors.addAll(response.contentExt.checkBlank("response.contentExt"))
            if (errors.isNotEmpty()) {
                log.error(
                    "\n${HttpOperation.SEPARATOR_LINE}\n${if (appName.isNullOrBlank()) "" else "$appName/"}${moduleName}/$collectionName/${name}：\n${
                        errors.joinToString(
                            "\n"
                        )
                    }\n${HttpOperation.SEPARATOR_LINE}"
                )
            }
        }

        private fun Iterable<Field>.checkBlank(
            desc: String,
            prefix: String = ""
        ): Iterable<String> {
            val errors = mutableListOf<String>()
            forEach {
                val blank = it.description.isBlank()
                if (blank) {
                    errors.add("[${desc}]未找到字段[${prefix + it.name}]的描述")
                }
                val childrenErrors = it.children.checkBlank(desc, "${prefix + it.name}.")
                errors.addAll(childrenErrors)
            }
            return errors
        }

        fun read(
            operationFile: File,
            collectionName: String = "",
            operationName: String = operationFile.nameWithoutExtension
        ): DocOperation? {
            return if (operationFile.exists()) {
                val docOperation = try {
                    AutodocUtil.yamlMapper.readValue(operationFile, DocOperation::class.java)
                } catch (e: Exception) {
                    log.error(collectionName + "/" + operationName + "解析失败")
                    throw e
                }
                docOperation.operationFile = operationFile
                docOperation.collectionName = collectionName
                docOperation.name = operationName
                if (docOperation.protocol.isBlank()) {
                    docOperation.protocol = RequestConverter.DEFAULT_PROTOCOL
                }
                docOperation.request.apply {
                    this as DocOperationRequest
                    uriVariables = uriVariablesExt.associate { Pair(it.name, it.value) }

                    headers = headersExt.associateTo(HttpHeaders()) { field ->
                        Pair(
                            field.name,
                            listOf(field.value)
                        )
                    }
                    queries = queriesExt.associateTo(Parameters()) { field ->
                        Pair(
                            field.name,
                            listOf(field.value)
                        )
                    }

                    var path = restUri
                    uriVariables.forEach { (t, u) ->
                        path = path.replace("{$t}", u)
                    }

                    uri =
                        URI("$path${if (queries.isNotEmpty()) "?${queries.toQueryString()}" else ""}")

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

    }
}