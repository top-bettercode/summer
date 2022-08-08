package top.bettercode.summer.util.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Audience(
    @field:JsonProperty("registration_id")
    var registrationId: List<String>
) {
    constructor(vararg registrationId: String) : this(registrationId.toList())
}