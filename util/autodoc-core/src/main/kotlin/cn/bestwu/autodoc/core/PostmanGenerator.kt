package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.autodoc.core.postman.*
import cn.bestwu.autodoc.core.postman.Collection
import cn.bestwu.logging.operation.HttpOperation
import cn.bestwu.logging.operation.Parameters
import cn.bestwu.logging.operation.QueryStringParser
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.io.File

/**
 *
 * @author Peter Wu
 */
object PostmanGenerator : AbstractbGenerator() {

    fun postman(autodoc: AutodocExtension) {
        val rootDoc = autodoc.rootSource
        val sourcePath = (rootDoc?.absoluteFile?.parentFile?.absolutePath
                ?: autodoc.source.absolutePath) + File.separator
        autodoc.listModules { module,pyname ->
            val postmanFile = autodoc.postmanFile(pyname)
            postmanFile.delete()
            postmanFile.parentFile.mkdirs()

            val variables = linkedSetOf<Variable>()
            variables.add(Variable(key = "apiHost", value = autodoc.apiHost, type = "string", description = "接口地址"))
            val items: List<Item> = module.collections.map { collection ->
                val collectionName = collection.name

                Item(name = collectionName, item = collection.operations.map { operation ->
                    autodoc.extFieldExt(operation)
                    val operationName = operation.operationFile.absolutePath.substringAfter(sourcePath)
                    val request = extractRequest(operation.request as DocOperationRequest, autodoc, operationName)

                    Item(name = operation.name, description = operation.description, request = request, response = listOf(extractResponse(operation.name, request, operation.response as DocOperationResponse, operationName)), event = module.postmanEvents(operation, autodoc))
                }.toList())
            }
            val postmanCollection = Collection(info = Info(name = autodoc.projectName), item = items, variable = variables.toList())
            postmanFile.writeText(postmanCollection.toJsonString())
            println("生成：$postmanFile")
        }
    }

    private fun extractRequest(request: DocOperationRequest, autodoc: AutodocExtension, operationName: String): Request {
        val httpHeaders = request.headers
        val headersKeys = httpHeaders.keys
        if (request.restUri != autodoc.authUri) {
            if ((headersKeys.contains("Authorization") || headersKeys.contains("authorization"))) {
                httpHeaders["Authorization"] = "{{token_type}} {{access_token}}"
            }
            autodoc.authVariables.forEach {
                val key = it.substringAfterLast('.')
                if (headersKeys.contains(key)) {
                    httpHeaders[key] = "{{$key}}"
                }
            }
        }
        if (headersKeys.contains(autodoc.signParam)) {
            httpHeaders[autodoc.signParam] = "{{${autodoc.signParam}}}"
        }

        httpHeaders.remove(HttpHeaders.HOST)
        httpHeaders.remove(HttpHeaders.CONTENT_LENGTH)
        val headerMap = httpHeaders.singleValueMap

        return Request(method = request.method.name, header = headerMap.toFields(request.headersExt, operationName).map {
            HeaderItem(it.name, it.name, it.value, it.postmanDescription)
        }, url = extractUrl(request, operationName), body = extractBody(request, operationName))
    }

    private fun extractResponse(name: String, request: Request, response: DocOperationResponse, operationName: String): Response {
        val httpHeaders = response.headers
        httpHeaders.remove(HttpHeaders.HOST)
        httpHeaders.remove(HttpHeaders.CONTENT_LENGTH)
        val headerMap = httpHeaders.singleValueMap
        return Response(name, request, response.statusCode, HttpStatus.valueOf(response.statusCode).reasonPhrase, headerMap.toFields(response.headersExt, operationName).map {
            HeaderItem(it.name, it.name, it.value)
        }, response.prettyContentAsString, postmanPreviewlanguage = "json")
    }

    private fun extractBody(request: DocOperationRequest, operationName: String): Body? {
        when {
            request.content.isNotEmpty() -> return Body("raw", raw = request.prettyContentAsString)
            request.parts.isNotEmpty() -> {
                return Body("formdata", formdata = request.parts.map {
                    val field = request.partsExt.findField(it.name, it.contentAsString.type, operationName)
                    Formdatum(it.name, field.value, if (it.submittedFileName == null) "text" else "file", field.postmanDescription)
                })
            }
            HttpOperation.isPutOrPost(request) -> {
                val param = request.parameters.singleValueMap.toMutableMap()
                if (param.keys.contains("refresh_token")) {
                    param["refresh_token"] = "{{refresh_token}}"
                }
                return Body("urlencoded", urlencoded = param.toFields(request.parametersExt, operationName).map {
                    Urlencoded(it.name, it.value, it.type.substringBefore("(").toLowerCase(), it.postmanDescription)
                })
            }
            else ->
                return null
        }
    }

    private fun extractUrl(request: DocOperationRequest, operationName: String): Url {
        val uri = request.restUri.replace("{", "{{").replace("}", "}}")
        return Url().apply {
            host = listOf("{{apiHost}}")
            path = uri.split("/").filter { it.isNotBlank() }
            raw = "{{apiHost}}${HttpOperation.getRestRequestPath(request)}"

            val queryString = request.uri.rawQuery
            val parameters = Parameters()
            if (!queryString.isNullOrBlank()) {
                parameters.addAll(QueryStringParser.parse(queryString))
            }
            if (request.parameters.isNotEmpty() && HttpOperation.includeParametersInUri(request)) {
                parameters.addAll(request.parameters)
            }
            query = parameters.singleValueMap.toFields(request.parametersExt, operationName).map {
                Query(it.name, it.value, it.postmanDescription)
            }
        }
    }
}