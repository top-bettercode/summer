package top.bettercode.summer.logging.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Channel(
        var id: String? = null,
        var name: String? = null
)
