package top.bettercode.summer.test.autodoc

/**
 *
 * @author Peter Wu
 */
data class ParamInfo(
        val requiredParameters: Set<String>,
        val defaultValueParams: MutableMap<String, String>,
        val existNoAnnoDefaultPageParam: Boolean
)