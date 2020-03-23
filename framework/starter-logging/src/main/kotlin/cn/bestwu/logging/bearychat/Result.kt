package cn.bestwu.logging.bearychat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
        val code: Int? = null,
        val result: String? = null
)
