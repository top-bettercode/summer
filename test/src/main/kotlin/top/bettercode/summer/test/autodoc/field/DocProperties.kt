package top.bettercode.summer.test.autodoc.field

/**
 *
 * @author Peter Wu
 */
enum class DocProperties(val propertyName: String) {

    REQUEST_HEADERS("request.headers"),
    REQUEST_PARAMETERS("request.parameters"),
    RESPONSE_HEADERS("response.headers"),
    RESPONSE_CONTENT("response.content");
}