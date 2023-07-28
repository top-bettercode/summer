package top.bettercode.summer.test.autodoc

/**
 *
 * @author Peter Wu
 */
data class ParamInfo(
        val requiredParameters: Set<String>,
        val defaultValueParams: Map<String, String>,
        val existNoAnnoDefaultPageParam: Boolean
)