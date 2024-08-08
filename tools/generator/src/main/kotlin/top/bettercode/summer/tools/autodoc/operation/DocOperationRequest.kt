package top.bettercode.summer.tools.autodoc.operation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.operation.OperationRequest

/**
 *
 * @author Peter Wu
 */
@JsonPropertyOrder(
    "queriesExt",
    "parametersExt",
    "contentExt",
    "uriVariablesExt",
    "partsExt",
    "headersExt",
    "restUri",
    "method",
    "cookies",
    "contentAsString"
)
@JsonIgnoreProperties(
    value = ["uri", "uriVariables", "headers", "queries", "parameters", "parts", "dateTime", "remoteUser", "username"],
    allowSetters = true
)
class DocOperationRequest(
    operationRequest: OperationRequest = OperationRequest(),
    /**
     * URI variables说明
     */
    var uriVariablesExt: LinkedHashSet<Field> = LinkedHashSet(),
    /**
     * 请求头说明
     */
    var headersExt: LinkedHashSet<Field> = LinkedHashSet(),
    /**
     * query参数说明
     */
    var queriesExt: LinkedHashSet<Field> = LinkedHashSet(),
    /**
     * form参数说明
     */
    var parametersExt: LinkedHashSet<Field> = LinkedHashSet(),
    /**
     * parts参数说明
     */
    var partsExt: LinkedHashSet<Field> = LinkedHashSet(),
    /**
     * 请求体参数说明
     */
    var contentExt: LinkedHashSet<Field> = LinkedHashSet()
) : OperationRequest(
    uri = operationRequest.uri,
    restUri = operationRequest.restUri,
    uriVariables = operationRequest.uriVariables,
    method = operationRequest.method,
    headers = operationRequest.headers,
    cookies = operationRequest.cookies,
    remoteUser = operationRequest.remoteUser,
    queries = operationRequest.queries,
    parameters = operationRequest.parameters,
    parts = operationRequest.parts,
    content = operationRequest.content,
    dateTime = operationRequest.dateTime
)