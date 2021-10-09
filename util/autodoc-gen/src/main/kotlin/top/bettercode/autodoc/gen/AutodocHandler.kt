package top.bettercode.autodoc.gen

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.method.HandlerMethod
import top.bettercode.api.sign.ApiSignProperties
import top.bettercode.autodoc.core.PostmanGenerator
import top.bettercode.autodoc.core.Util
import top.bettercode.autodoc.core.model.DocModule
import top.bettercode.autodoc.core.model.Field
import top.bettercode.autodoc.core.operation.DocOperation
import top.bettercode.autodoc.core.operation.DocOperationRequest
import top.bettercode.autodoc.core.operation.DocOperationResponse
import top.bettercode.autodoc.core.singleValueMap
import top.bettercode.autodoc.core.toMap
import top.bettercode.generator.GeneratorExtension
import top.bettercode.logging.RequestLoggingHandler
import top.bettercode.logging.operation.Operation
import top.bettercode.simpleframework.config.SummerWebProperties
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.annotation.PreDestroy

/**
 * @author Peter Wu
 */
class AutodocHandler(
    private val genProperties: GenProperties,
    private val signProperties: ApiSignProperties,
    private val summerWebProperties: SummerWebProperties
) : RequestLoggingHandler {

    private val log: Logger = LoggerFactory.getLogger(AutodocHandler::class.java)
    private val cache: ConcurrentMap<File, DocModule> = ConcurrentHashMap()

    @Suppress("unused")
    @PreDestroy
    fun destroy() {
        try {
            cache.values.forEach { it.writeToDisk() }
            if (genProperties.doc) {
                top.bettercode.autodoc.core.AsciidocGenerator.asciidoc(genProperties)
                PostmanGenerator.postman(genProperties)
                top.bettercode.autodoc.core.AsciidocGenerator.html(genProperties)
            }
        } catch (e: Exception) {
            log.error("生成文档失败", e)
        }
    }

    @Synchronized
    override fun handle(operation: Operation, handler: HandlerMethod?) {
        if (Autodoc.enable) {
            try {
                if (operation.response.stackTrace.isNotBlank() && operation.response.content.isEmpty()) {
                    return
                }
                //生成相应数据
                val projectModuleDic = File(
                    genProperties.source,
                    Autodoc.version.ifBlank { genProperties.version }
                )
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
                    operation.request.restUri =
                        "/${genProperties.projectPath}${operation.request.restUri}"
                    val uri = operation.request.uri
                    operation.request.uri = URI(
                        uri.scheme,
                        uri.authority,
                        operation.request.restUri,
                        uri.query,
                        uri.fragment
                    )
                }

                val docOperation = operation.description(projectModuleDic, Autodoc.description)

                val request = docOperation.request as DocOperationRequest

                //headers
                val calculateHeaders = RequiredParameters.calculateHeaders(handler)
                val defaultValueHeaders =
                    calculateHeaders.filter { it.value != ValueConstants.DEFAULT_NONE }
                val requiredHeaders = calculateHeaders.keys.toMutableSet()
                requiredHeaders.addAll(Autodoc.requiredHeaders)
                val signParamName = signProperties.parameterName
                if (signProperties.requiredSign(handler))
                    requiredHeaders.add(signParamName)
                else
                    request.headers.remove(signParamName)

                if (!requiredHeaders.contains("Authorization")) {
                    request.headers.remove("Authorization")
                }

                request.headers.remove(HttpHeaders.HOST)
                request.headers.remove(HttpHeaders.CONTENT_LENGTH)
                request.headers.remove(HttpHeaders.CONNECTION)
                request.headers.remove(HttpHeaders.USER_AGENT)

                val headers = HttpHeaders()
                headers.putAll(request.headers)
                (setOf("Accept", "Content-Type") + Autodoc.headers + requiredHeaders).forEach {
                    val defaultValue = when (it) {
                        "Accept" -> listOf(MediaType.APPLICATION_JSON_VALUE)
                        "Content-Type" -> listOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        else -> listOf("")
                    }
                    headers[it] = request.headers.getOrDefault(it, defaultValue)
                }
                request.headers.clear()
                request.headers.putAll(headers)

                //参数
                val calculateParams = RequiredParameters.calculate(handler)
                val defaultValueParams =
                    calculateParams.filter { it.value != ValueConstants.DEFAULT_NONE }
                val requiredParameters = calculateParams.keys.toMutableSet()
                requiredParameters.addAll(Autodoc.requiredParameters)

                if (requiredHeaders.contains(signParamName)) {
                    docOperation.prerequest = prerequestExec(docOperation, signProperties)
                }

                //collections
                module.collections(docOperation.collectionName, docOperation.name)

                //field
                val extension = GeneratorExtension(
                    datasource = genProperties.datasource,
                    dataType = genProperties.dataType,
                    tablePrefixes = genProperties.tablePrefixes
                )

                request.uriVariablesExt = request.uriVariables.toFields(request.uriVariablesExt)
                request.headersExt = request.headers.singleValueMap.toFields(request.headersExt)
                request.headersExt.forEach {
                    it.required = requiredHeaders.contains(it.name)
                }

                request.parametersExt =
                    request.parameters.singleValueMap.toFields(request.parametersExt, expand = true)
                request.parametersExt.forEach {
                    setRequired(it, requiredParameters)
                }

                request.partsExt = request.parts.toFields(request.partsExt)
                request.partsExt.forEach {
                    setRequired(it, requiredParameters)
                }

                request.contentExt =
                    request.contentAsString.toMap()?.toFields(request.contentExt, expand = true)
                        ?: linkedSetOf()
                request.contentExt.forEach {
                    setRequired(it, requiredParameters)
                }

                val response = docOperation.response as DocOperationResponse
                response.headersExt = response.headers.singleValueMap.toFields(response.headersExt)
                response.contentExt =
                    response.contentAsString.toMap()?.toFields(response.contentExt, expand = true)
                        ?: linkedSetOf()

                InitField.extFieldExt(genProperties, docOperation)
                InitField.init(
                    docOperation,
                    extension,
                    genProperties.allTables,
                    summerWebProperties.wrapEnable,
                    defaultValueHeaders,
                    defaultValueParams
                )

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

    private fun setRequired(
        field: Field,
        requiredParameters: MutableSet<String>,
        prefix: String = ""
    ) {
        field.required = requiredParameters.contains(prefix + field.name)
        if (field.children.isNotEmpty()) {
            field.children.forEach {
                setRequired(it, requiredParameters, "${prefix + field.name}.")
            }
        }
    }


    private fun Operation.description(dir: File, description: String): DocOperation {
        val operationFile = File(dir, "collection/$collectionName/$name.yml")
        var docOperation: DocOperation? = null
        if (!operationFile.exists()) {
            val subDirs = dir.parentFile.listFiles()
            if (subDirs != null) {
                for (oDir in subDirs) {
                    if (!oDir.equals(dir)) {
                        val oldOperationFile = File(oDir, "collection/$collectionName/$name.yml")
                        if (oldOperationFile.exists()) {
                            docOperation =
                                Util.yamlMapper.readValue(
                                    oldOperationFile,
                                    DocOperation::class.java
                                )
                            break
                        }
                    }
                }
            }
        } else {
            docOperation = Util.yamlMapper.readValue(operationFile, DocOperation::class.java)
        }
        return if (docOperation != null) {
            docOperation.description = description
            docOperation.operationFile = operationFile
            val operation = this
            docOperation.apply {
                collectionName = operation.collectionName
                name = operation.name
                protocol = operation.protocol
                val existReq = request as DocOperationRequest
                request = DocOperationRequest(
                    operationRequest = operation.request,
                    uriVariablesExt = existReq.uriVariablesExt,
                    headersExt = existReq.headersExt,
                    parametersExt = existReq.parametersExt,
                    partsExt = existReq.partsExt,
                    contentExt = existReq.contentExt
                )
                val existRes = response as DocOperationResponse
                response = DocOperationResponse(
                    operationResponse = operation.response,
                    headersExt = existRes.headersExt,
                    contentExt = existRes.contentExt
                )
            }
            docOperation
        } else {
            docOperation = DocOperation(this, description)
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
    exec.addAll(
        StreamUtils.copyToString(
            AutodocHandler::class.java.getResourceAsStream("/sign.js"),
            charset("UTF-8")
        ).lines()
    )
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
