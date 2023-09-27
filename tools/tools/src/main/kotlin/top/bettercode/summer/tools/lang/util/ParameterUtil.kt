package top.bettercode.summer.tools.lang.util

import org.springframework.util.MultiValueMap

/**
 * 参数工具类
 *
 * @author Peter Wu
 */
object ParameterUtil {

    /**
     * @param params 容器
     * @param key 参数名
     * @return 是否有名为key且内容不为空的参数
     */
    @JvmStatic
    fun hasParameter(params: Map<String, Array<String>>, key: String): Boolean {
        return params.containsKey(key) && !params[key]?.get(0).isNullOrBlank()
    }

    /**
     * @param params 容器
     * @param key 参数名
     * @return 是否有名为key的参数
     */
    @JvmStatic
    fun hasParameterKey(params: Map<String, Array<String>>, key: String): Boolean {
        return params.containsKey(key)
    }

    /**
     * @param params 容器
     * @param key 参数名
     * @return 是否有名为key且内容不为空的参数
     */
    @JvmStatic
    fun hasParameter(params: MultiValueMap<String, String>, key: String): Boolean {
        return params.containsKey(key) && !params.getFirst(key).isNullOrBlank()
    }

    /**
     * @param params 容器
     * @param key 参数名
     * @return 是否有名为key的参数
     */
    @JvmStatic
    fun hasParameterKey(params: MultiValueMap<String, String>, key: String): Boolean {
        return params.containsKey(key)
    }

}
