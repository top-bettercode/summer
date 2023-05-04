package top.bettercode.summer.security.authorize

import org.springframework.security.access.SecurityConfig
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

/**
 * @author Peter Wu
 */
object DefaultAuthority {
    const val DEFAULT_AUTHENTICATED_VALUE = "authenticated"
    var DEFAULT_GRANTED_AUTHORITY: GrantedAuthority = SimpleGrantedAuthority(
            DEFAULT_AUTHENTICATED_VALUE)
    var DEFAULT_AUTHENTICATED = SecurityConfig(
            DEFAULT_AUTHENTICATED_VALUE)
    val ROLE_ANONYMOUS = SecurityConfig(Anonymous.ROLE_ANONYMOUS_VALUE)
    fun isDefaultAuthority(authority: String): Boolean {
        return DEFAULT_GRANTED_AUTHORITY.authority == authority
    }

    fun isDefaultAuthority(authority: GrantedAuthority): Boolean {
        return DEFAULT_GRANTED_AUTHORITY == authority
    }

    fun defaultAuthority(): Collection<GrantedAuthority> {
        return setOf(DEFAULT_GRANTED_AUTHORITY)
    }

    fun addDefaultAuthority(
            vararg authorities: GrantedAuthority
    ): Collection<GrantedAuthority> {
        val objects = HashSet(listOf(*authorities))
        objects.add(DEFAULT_GRANTED_AUTHORITY)
        return objects
    }

    fun addDefaultAuthority(
            vararg authorities: String?
    ): Collection<GrantedAuthority> {
        val objects = HashSet<GrantedAuthority>()
        for (authority in authorities) {
            objects.add(SimpleGrantedAuthority(authority))
        }
        objects.add(DEFAULT_GRANTED_AUTHORITY)
        return objects
    }

    fun addDefaultAuthority(
            authorities: Collection<String?>
    ): Collection<GrantedAuthority> {
        val objects = HashSet<GrantedAuthority>()
        for (authority in authorities) {
            objects.add(SimpleGrantedAuthority(authority))
        }
        objects.add(DEFAULT_GRANTED_AUTHORITY)
        return objects
    }
}
