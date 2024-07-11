package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class FeishuDataResult<T> : FeishuResult() {
    var data: T? = null
}