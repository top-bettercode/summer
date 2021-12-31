package top.bettercode.autodoc.core

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import top.bettercode.autodoc.core.operation.DocOperationRequest
import top.bettercode.autodoc.core.operation.DocOperationResponse
import top.bettercode.autodoc.core.postman.*
import top.bettercode.autodoc.core.postman.Collection
import top.bettercode.logging.operation.HttpOperation
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
object PostmanGenerator : top.bettercode.autodoc.core.AbstractbGenerator() {

    fun postman(autodoc: AutodocExtension) {
        val rootDoc = autodoc.rootSource
        val sourcePath = (rootDoc?.absoluteFile?.parentFile?.absolutePath
            ?: autodoc.source.absolutePath) + File.separator
        autodoc.listModules { module, pyname ->
            val postmanFile = autodoc.postmanFile(pyname)
            postmanFile.delete()
            postmanFile.parentFile.mkdirs()

            val variables = linkedSetOf<Variable>()
            variables.add(
                Variable(
                    key = "apiHost",
                    value = "http://localhost:8080",
                    type = "string",
                    description = "接口地址"
                )
            )
            val items: List<Item> = module.collections.map { collection ->
                val collectionName = collection.name

                Item(name = collectionName, item = collection.operations.map { operation ->
                    val operationPath =
                        operation.operationFile.absolutePath.substringAfter(sourcePath)
                    val request = extractRequest(
                        operation.request as DocOperationRequest,
                        autodoc,
                        operationPath
                    )

                    Item(
                        name = operation.name,
                        description = operation.description,
                        request = request,
                        response = listOf(
                            extractResponse(
                                operation.name,
                                request,
                                operation.response as DocOperationResponse,
                                operationPath
                            )
                        ),
                        event = module.postmanEvents(operation, autodoc)
                    )
                }.toList())
            }
            val postmanCollection = Collection(
                info = Info(name = autodoc.projectName + " " + pyname),
                item = items,
                variable = variables.toList()
            )
            postmanFile.writeText(postmanCollection.toJsonString())
            println("生成：$postmanFile")
        }
    }

    private fun extractRequest(
        request: DocOperationRequest,
        autodoc: AutodocExtension,
        operationPath: String
    ): Request {
        val httpHeaders = request.headersExt
        if (request.restUri != autodoc.authUri) {
            httpHeaders.filter { it.name == "Authorization" || it.name == "authorization" }
                .forEach {
                    it.value = "{{token_type}} {{access_token}}"
                }
            httpHeaders.filter { autodoc.authVariables.contains(it.name) }.forEach {
                it.value = "{{${it.name}}}"
            }
        }
        httpHeaders.filter { autodoc.signParam == it.name }.forEach {
            it.value = "{{${it.name}}}"
        }


        httpHeaders.removeIf { it.name == HttpHeaders.HOST || it.name == HttpHeaders.CONTENT_LENGTH }

        return Request(
            method = request.method.name,
            header = request.headersExt.checkBlank("$operationPath:request.headersExt").map {
                HeaderItem(it.name, it.name, it.value, it.postmanDescription)
            },
            url = extractUrl(request, operationPath),
            body = extractBody(request, operationPath)
        )
    }

    private fun extractResponse(
        name: String,
        request: Request,
        response: DocOperationResponse,
        operationPath: String
    ): Response {
        val httpHeaders = response.headersExt
        httpHeaders.removeIf { it.name == HttpHeaders.HOST || it.name == HttpHeaders.CONTENT_LENGTH }

        val contentType = response.headers.contentType
        return Response(
            name,
            request,
            response.statusCode,
            HttpStatus.valueOf(response.statusCode).reasonPhrase,
            httpHeaders.checkBlank("$operationPath:response.headersExt").map {
                HeaderItem(it.name, it.name, it.value)
            },
            response.prettyContentAsString,
            postmanPreviewlanguage = when {
                MediaType.APPLICATION_JSON
                    .isCompatibleWith(contentType) -> "json"
                MediaType.APPLICATION_XML
                    .isCompatibleWith(contentType) -> "xml"
                else -> "text"
            }
        )
    }

    private fun extractBody(request: DocOperationRequest, operationPath: String): Body? {
        when {
            request.contentExt.isNotEmpty() -> return Body(
                "raw",
                raw = request.prettyContentAsString
            )
            request.partsExt.isNotEmpty() -> {
                return Body(
                    "formdata",
                    formdata = request.partsExt.checkBlank("$operationPath:request.partsExt").map {
                        Formdatum(it.name, it.value, it.partType, it.postmanDescription)
                    })
            }
            HttpOperation.isPutOrPost(request) -> {
                val param = request.parametersExt.checkBlank("$operationPath:request.parametersExt")
                param.filter { it.name == "refresh_token" }.forEach {
                    it.value = "{{refresh_token}}"
                }

                return Body("urlencoded", urlencoded = param.map {
                    Urlencoded(
                        it.name,
                        it.value,
                        it.type.substringBefore("(").lowercase(Locale.getDefault()),
                        it.postmanDescription
                    )
                })
            }
            else ->
                return null
        }
    }

    private fun extractUrl(request: DocOperationRequest, operationPath: String): Url {
        val uri = request.restUri.replace("{", "{{").replace("}", "}}")
        return Url().apply {
            host = listOf("{{apiHost}}")
            path = uri.split("/").filter { it.isNotBlank() }
            raw = "{{apiHost}}${HttpOperation.getRestRequestPath(request)}"

            query = request.parametersExt.checkBlank("$operationPath:request.parametersExt").map {
                Query(it.name, it.value, it.postmanDescription)
            }
        }
    }
}