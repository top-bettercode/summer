package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class FeishuDataResult<T> : FeishuResult() {
    var data: T? = null
}