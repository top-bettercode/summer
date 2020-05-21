package cn.bestwu.autodoc.gen

import cn.bestwu.api.sign.ApiSignProperties
import cn.bestwu.autodoc.core.*
import cn.bestwu.autodoc.core.model.DocModule
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.operation.DocOperationRequest
import cn.bestwu.autodoc.core.operation.DocOperationResponse
import cn.bestwu.generator.GeneratorExtension
import cn.bestwu.logging.RequestLoggingHandler
import cn.bestwu.logging.operation.Operation
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.method.HandlerMethod
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.annotation.PreDestroy

/**
 * @author Peter Wu
 */
class AutodocHandler(private val genProperties: GenProperties, private val signProperties: ApiSignProperties, private val wrap: Boolean) : RequestLoggingHandler {

    private val log: Logger = LoggerFactory.getLogger(AutodocHandler::class.java)
    private val cache: ConcurrentMap<File, DocModule> = ConcurrentHashMap()

    @PreDestroy
    fun destroy() {
        try {
            cache.values.forEach { it.writeToDisk() }
            if (genProperties.doc) {
                AsciidocGenerator.asciidoc(genProperties)
                PostmanGenerator.postman(genProperties)
                AsciidocGenerator.html(genProperties)
            }
        } catch (e: Exception) {
            log.error("生成文档失败", e)
        }
    }

    @Synchronized
    override fun handle(operation: Operation, handler: HandlerMethod?) {
        if (Autodoc.enable) {
            try {
                if (operation.response.stackTrace.isNotBlank()) {
                    return
                }
                //生成相应数据
                val projectModuleDic = File(genProperties.source, genProperties.version)
                projectModuleDic.mkdirs()
                val module = cache.getOrPut(projectModuleDic) {
                    DocModule(null, projectModuleDic)
                }
                if (Autodoc.name.isNotBlank()) {
                    operation.name = Autodoc.name
                }
                if (Autodoc.collectionName.isNotBlank()) {
                    operation.collectionName = Autodoc.collectionName
                }

                if (operation.name.isBlank()) {
                    log.warn("docOperation name未设置")
                    return
                }

                if (operation.collectionName.isBlank()) {
                    log.warn("docOperation resource未设置")
                    return
                }
                operation.collectionName = operation.collectionName.replace("/", "、")
                operation.name = operation.name.replace("/", "、")

                if (genProperties.projectPath.isNotBlank()) {
                    operation.request.restUri = "/${genProperties.projectPath}${operation.request.restUri}"
                    val uri = operation.request.uri
                    operation.request.uri = URI(uri.scheme, uri.authority, operation.request.restUri, uri.query, uri.fragment)
                }

                val docOperation = operation.description(projectModuleDic, Autodoc.description)

                val request = docOperation.request as DocOperationRequest

                val requiredHeaders = RequiredParameters.calculateHeaders(handler)
                requiredHeaders.addAll(Autodoc.requiredHeaders)

                request.headers.remove(HttpHeaders.HOST)
                request.headers.remove(HttpHeaders.CONTENT_LENGTH)
                request.headers.remove(HttpHeaders.CONNECTION)
                request.headers.remove(HttpHeaders.USER_AGENT)

                val headers = HttpHeaders()
                (setOf("Accept", "Content-Type") + Autodoc.headers + requiredHeaders).forEach {
                    val defaultValue = when (it) {
                        "Accept" -> listOf(MediaType.APPLICATION_JSON_VALUE)
                        "Content-Type" -> listOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        else -> listOf("")
                    }
                    headers[it] = request.headers.getOrDefault(it, defaultValue)
                }
                headers.putAll(request.headers)
                request.headers = headers

                val requiredParameters = RequiredParameters.calculate(handler)
                requiredParameters.addAll(Autodoc.requiredParameters)
                requiredParameters.forEach {
                    request.parameters[it] = request.parameters[it] ?: listOf()
                }

                val signName = signProperties.parameterName
                if (requiredHeaders.contains(signName)) {
                    docOperation.prerequest = prerequestExec(docOperation, signProperties)
                }

                //collections
                module.collections(docOperation.collectionName, docOperation.name)

                //field
                val extension = GeneratorExtension(datasource = genProperties.datasource, dataType = genProperties.dataType, tablePrefix = genProperties.tablePrefix)

                request.uriVariablesExt = request.uriVariables.toFields(request.uriVariablesExt)
                request.headersExt = request.headers.singleValueMap.toFields(request.headersExt)
                request.headersExt.forEach {
                    it.required = requiredHeaders.contains(it.name)
                }

                request.partsExt = request.parts.toFields(request.partsExt)
                request.partsExt.forEach {
                    it.required = requiredParameters.contains(it.name)
                }

                request.parametersExt = request.parameters.singleValueMap.filter { p -> request.partsExt.none { p.key == it.name } }.toFields(request.parametersExt, expand = true)
                request.parametersExt.forEach {
                    it.required = requiredParameters.contains(it.name)
                }

                request.contentExt = request.contentAsString.toMap()?.toFields(request.contentExt, expand = true)
                        ?: linkedSetOf()

                val response = docOperation.response as DocOperationResponse
                response.headersExt = response.headers.singleValueMap.toFields(response.headersExt)
                response.contentExt = response.contentAsString.toMap()?.toFields(response.contentExt, expand = true)
                        ?: linkedSetOf()

                InitField.extFieldExt(genProperties, docOperation)
                InitField.init(docOperation, extension, genProperties.allTables, wrap)

                docOperation.save()
            } finally {
                Autodoc.collectionName = ""
                Autodoc.name = ""
                Autodoc.tableNames = setOf()
                Autodoc.requiredParameters = setOf()
                Autodoc.requiredHeaders = setOf()
                Autodoc.headers = setOf()
                Autodoc.enable = true
                Autodoc.description = ""
                Autodoc.schema = null
            }
        }
    }

    @JsonIgnore
    private fun Operation.description(dir: File, description: String): DocOperation {
        val operationFile = File(dir, "collection/$collectionName/$name.yml")
        return if (operationFile.exists()) {
            val exist = Util.yamlMapper.readValue(operationFile, DocOperation::class.java)
            exist.description = description
            exist.operationFile = operationFile
            val operation = this
            exist.apply {
                collectionName = operation.collectionName
                name = operation.name
                protocol = operation.protocol
                val existReq = request as DocOperationRequest
                request = DocOperationRequest(operation.request, existReq.uriVariablesExt, existReq.headersExt, existReq.parametersExt, existReq.partsExt, existReq.contentExt)
                val existRes = response as DocOperationResponse
                response = DocOperationResponse(operation.response, existRes.headersExt, existRes.contentExt)
            }
            exist
        } else {
            val docOperation = DocOperation(this, description)
            docOperation.operationFile = operationFile
            docOperation
        }
    }


}

/**
 * @param operation operation
 */
internal fun prerequestExec(operation: Operation, signProperties: ApiSignProperties): List<String> {
    val exec = mutableListOf<String>()
    operation.request.apply {
        operation.request.uriVariables.forEach { (t, u) ->
            exec.add("pm.globals.set('$t', '$u');")
        }
    }
    exec.addAll(StreamUtils.copyToString(AutodocHandler::class.java.getResourceAsStream("/sign.js"), charset("UTF-8")).lines())
    exec.add("signClient({")
    exec.add("    clientSecret: '${signProperties.clientSecret}'")
    exec.add("});")
    exec.add("var params;")
    exec.add("if (pm.request.method === 'GET' || pm.request.method === 'DELETE') {")
    exec.add("    params = pm.request.url.getQueryString();")
    exec.add("} else {")
    exec.add("    params = pm.request.body.urlencoded.map(function (it) { return it.key + '=' + it.value }).join('&');")
    exec.add("}")
    exec.add("pm.globals.set('sign', signClient.sign(params));")
    return exec
}
