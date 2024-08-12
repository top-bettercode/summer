package top.bettercode.summer.gradle.plugin.autodoc

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import top.bettercode.summer.tools.autodoc.AutodocExtension
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.autodoc.postman.*
import top.bettercode.summer.tools.autodoc.postman.Collection
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.StringUtil.toJsonString
import java.util.*

/**
 *
 * @author Peter Wu
 */
object PostmanGenerator {
    private val log = LoggerFactory.getLogger(PostmanGenerator::class.java)

    fun postman(autodoc: AutodocExtension) {

        autodoc.listModules { module, pyname ->
            val postmanFile = autodoc.postmanFile(pyname)
            postmanFile.delete()
            postmanFile.parentFile.mkdirs()

            val variables = linkedSetOf<Variable>()
            variables.add(
                Variable(
                    key = "apiAddress",
                    value = autodoc.apiAddress,
                    type = "string",
                    description = "接口地址"
                )
            )
            val items: List<Item> = module.collections.map { collection ->
                val collectionName = collection.name

                Item(name = collectionName, item = collection.operations.map { operation ->
                    val request = extractRequest(
                        operation.request as DocOperationRequest,
                        autodoc,
                    )

                    Item(
                        name = operation.name,
                        description = operation.description,
                        request = request,
                        response = listOf(
                            extractResponse(
                                operation.name,
                                request,
                                operation.response as DocOperationResponse
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
            log.warn("生成：$postmanFile")
        }
    }

    private fun extractRequest(
        request: DocOperationRequest,
        autodoc: AutodocExtension,
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
            method = request.method,
            header = request.headersExt.map {
                HeaderItem(it.name, it.name, it.value, it.postmanDescription)
            },
            url = extractUrl(request),
            body = extractBody(request)
        )
    }

    private fun extractResponse(
        name: String,
        request: Request,
        response: DocOperationResponse
    ): Response {
        val httpHeaders = response.headersExt
        httpHeaders.removeIf { it.name == HttpHeaders.HOST || it.name == HttpHeaders.CONTENT_LENGTH }

        val contentType = response.contentType
        return Response(
            name = name,
            originalRequest = request,
            code = response.statusCode,
            status = try {
                HttpStatus.valueOf(response.statusCode).reasonPhrase
            } catch (e: Exception) {
                HttpStatus.OK.reasonPhrase
            },
            header = httpHeaders.map {
                HeaderItem(it.name, it.name, it.value)
            },
            body = response.prettyContentAsString,
            postmanPreviewlanguage = when {
                MediaType.APPLICATION_JSON
                    .isCompatibleWith(contentType) -> "json"

                MediaType.APPLICATION_XML
                    .isCompatibleWith(contentType) -> "xml"

                else -> "text"
            }
        )
    }

    private fun extractBody(request: DocOperationRequest): Body? {
        when {
            request.contentExt.isNotEmpty() -> return Body(
                "raw",
                raw = request.prettyContentAsString,
                options = when {
                    MediaType.APPLICATION_JSON
                        .isCompatibleWith(request.contentType) -> Body.rawLanguage()

                    MediaType.APPLICATION_XML
                        .isCompatibleWith(request.contentType) -> Body.rawLanguage("xml")

                    else -> Body.rawLanguage("text")
                }
            )

            request.partsExt.isNotEmpty() -> {
                return Body(
                    "formdata",
                    formdata = request.partsExt.map {
                        Formdatum(it.name, it.value, it.partType, it.postmanDescription)
                    })
            }

            request.parametersExt.isNotEmpty() -> {
                val param = request.parametersExt
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

    private fun extractUrl(request: DocOperationRequest): Url {
        val uri = request.restUri.replace("{", "{{").replace("}", "}}")
        return Url().apply {
            host = listOf("{{apiAddress}}")
            path = uri.split("/").filter { it.isNotBlank() }
            raw = "{{apiAddress}}${HttpOperation.getRequestPath(request)}"

            query = request.queriesExt.map {
                Query(it.name, it.value, it.postmanDescription)
            }
        }
    }
}