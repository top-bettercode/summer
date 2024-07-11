package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class FeishuFile {
    @JsonProperty("file_key")
    var fileKey: String? = null
}