package top.bettercode.summer.test.autodoc

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.method.HandlerMethod
import top.bettercode.summer.apisign.ApiSignProperties
import top.bettercode.summer.logging.RequestLoggingHandler
import top.bettercode.summer.test.autodoc.field.FieldDescFix
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.model.DocModule
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.operation.DocOperationRequest
import top.bettercode.summer.tools.autodoc.operation.DocOperationResponse
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.lang.CharSequenceExtensions.capitalized
import top.bettercode.summer.tools.lang.operation.Operation
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.properties.SummerWebProperties
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.annotation.PreDestroy

/**
 * @author Peter Wu
 */
class AutodocHandler(
    private val datasources: Map<String, DatabaseConfiguration>,
    private val genProperties: GenProperties,
    private val signProperties: ApiSignProperties,
    private val summerWebProperties: SummerWebProperties,
    private val springDataWebProperties: SpringDataWebProperties?,
    private val autodocAspect: AutodocAspect?
) : RequestLoggingHandler {

    private val log: Logger = LoggerFactory.getLogger(AutodocHandler::class.java)
    private val cache: ConcurrentMap<File, DocModule> = ConcurrentHashMap()

    @PreDestroy
    fun destroy() {
        try {
            cache.values.forEach { it.writeToDisk() }
        } catch (e: Exception) {
            log.error("生成文档失败", e)
        }
    }

    @Synchronized
    override fun handle(operation: Operation, handler: HandlerMethod?) {
        if (Autodoc.enable) {
            try {
                val disableOnException =
                    Autodoc.disableOnException ?: genProperties.isDisableOnException
                if (disableOnException && operation.response.stackTrace.isNotBlank()) {
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
                operation.collectionName =
                    operation.collectionName.replace("/", AutodocUtil.REPLACE_CHAR)
                operation.name = operation.name.replace("/", AutodocUtil.REPLACE_CHAR)

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

                Autodoc.ignoredHeaders.forEach {
                    request.headers.remove(it)
                }

                genProperties.ignoredHeaders.forEach {
                    request.headers.remove(it)
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
                val paramInfo = RequiredParameters.calculate(handler)
                val defaultValueParams = paramInfo.defaultValueParams
                val pageable = springDataWebProperties?.pageable
                if (pageable != null)
                    defaultValueParams[pageable.sizeParameter] = pageable.defaultPageSize.toString()
                val requiredParameters = paramInfo.requiredParameters.toMutableSet()
                requiredParameters.addAll(Autodoc.requiredParameters)

                if (requiredHeaders.contains(signParamName)) {
                    docOperation.prerequest = prerequestExec(docOperation, signProperties)
                }

                //collections
                module.projectCollections(docOperation.collectionName, docOperation.name)

                DocExt.ext(
                    docOperation = docOperation,
                    requiredHeaders = requiredHeaders,
                    requiredParameters = requiredParameters,
                    defaultValueHeaders = defaultValueHeaders,
                    defaultValueParams = defaultValueParams
                )

                val extension = GeneratorExtension()
                extension.databases = datasources

                val beanType = handler?.beanType
                extTableNames(beanType)
                FieldDescFix.fix(
                    operation = docOperation,
                    extension = extension,
                    module = module
                )

                if (paramInfo.existNoAnnoDefaultPageParam) {
                    (docOperation.request as DocOperationRequest).parametersExt.filter { "page" == it.name || "size" == it.name }
                        .forEach {
                            it.description += "，不传返回所有数据"
                            it.defaultVal = ""
                        }
                }

                docOperation.save()
            } finally {
                Autodoc.reset()
            }
        }
    }

    private fun extTableNames(beanType: Class<*>?) {
        if (beanType != null) {
            var entityTypeName = beanType.simpleName.substringBeforeLast("Controller")
            val projectName = summerWebProperties.projectName?.capitalized()
            if (!projectName.isNullOrBlank() && entityTypeName.endsWith(projectName)) {
                entityTypeName = entityTypeName.substringBeforeLast(projectName)
            }
            val entityTypeNames = linkedSetOf<String>()
            if (isEntity(entityTypeName)) {
                entityTypeNames.add(entityTypeName)
            }
            beanType.declaredFields.forEach {
                var otherEntityTypeName = it.type.simpleName
                if (otherEntityTypeName.endsWith("Service")) {
                    otherEntityTypeName = otherEntityTypeName.substringBeforeLast("Service")
                    if (!projectName.isNullOrBlank() && otherEntityTypeName.endsWith(projectName)) {
                        otherEntityTypeName = otherEntityTypeName.substringBeforeLast(projectName)
                    }
                    if (otherEntityTypeName.startsWith("I") && otherEntityTypeName[1].isUpperCase()) {
                        otherEntityTypeName = otherEntityTypeName.substring(1)
                    }
                    if (isEntity(otherEntityTypeName)) {
                        entityTypeNames.add(otherEntityTypeName)
                    } else if (otherEntityTypeName.endsWith("Core")) {
                        otherEntityTypeName = otherEntityTypeName.substringBeforeLast("Core")
                        if (isEntity(otherEntityTypeName)) {
                            entityTypeNames.add(otherEntityTypeName)
                        }
                    }
                }
            }
            entityTypeNames.removeAll(Autodoc.tableNames)
            entityTypeNames.removeAll(Autodoc.fields.keys)
            if (entityTypeNames.isNotEmpty()) {
                if (log.isDebugEnabled)
                    log.debug("自动增加可能参数类型：{}", entityTypeNames)
                Autodoc.tableNames.addAll(entityTypeNames)
            }
        }
    }

    private fun isEntity(typeName: String): Boolean {
        return autodocAspect?.isEntity(typeName) ?: true
    }


    private fun Operation.description(dir: File, description: String): DocOperation {
        val operationFile = File(dir, "collection/$collectionName/$name.yml")
        var docOperation: DocOperation? = null
        if (!operationFile.exists()) {
            val subDirs = dir.parentFile.listFiles()?.filter { it.isDirectory }
            if (subDirs != null) {
                subDirs.sortedWith { o1, o2 ->
                    StringUtil.compareVersion(
                        o2.nameWithoutExtension,
                        o1.nameWithoutExtension,
                    )
                }
                for (oDir in subDirs) {
                    if (!oDir.equals(dir)) {
                        val oldOperationFile = File(oDir, "collection/$collectionName/$name.yml")
                        if (oldOperationFile.exists()) {
                            docOperation =
                                AutodocUtil.yamlMapper.readValue(
                                    oldOperationFile,
                                    DocOperation::class.java
                                )
                            break
                        }
                    }
                }
            }
        } else {
            docOperation = AutodocUtil.yamlMapper.readValue(operationFile, DocOperation::class.java)
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


    companion object {

        /**
         * @param operation operation
         */
        fun prerequestExec(
            operation: Operation,
            signProperties: ApiSignProperties
        ): List<String> {
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
    }
}

