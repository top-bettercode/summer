package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
class FeishuFile {
    @JsonProperty("file_key")
    var fileKey: String? = null
}