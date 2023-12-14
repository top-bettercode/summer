package top.bettercode.summer.security.userdetails

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

open class AdditionalUserDetails : User {

    val additionalInformation: MutableMap<String, Any?> = HashMap()

    constructor(
            username: String?, password: String?,
            authorities: Collection<GrantedAuthority?>?
    ) : super(username, password, authorities)

    constructor(
            username: String?, password: String?, enabled: Boolean,
            accountNonExpired: Boolean,
            credentialsNonExpired: Boolean, accountNonLocked: Boolean,
            authorities: Collection<GrantedAuthority?>?
    ) : super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
            authorities)

    fun put(key: String, value: Any?) {
        additionalInformation[key] = value
    }

    operator fun get(key: String): Any? {
        return additionalInformation[key]
    }

}
