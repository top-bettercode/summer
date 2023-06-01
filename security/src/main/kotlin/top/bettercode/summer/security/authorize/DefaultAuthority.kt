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

    @JvmField
    var DEFAULT_GRANTED_AUTHORITY: GrantedAuthority = SimpleGrantedAuthority(
            DEFAULT_AUTHENTICATED_VALUE)

    @JvmField
    var DEFAULT_AUTHENTICATED = SecurityConfig(DEFAULT_AUTHENTICATED_VALUE)

    @JvmField
    val ROLE_ANONYMOUS = SecurityConfig(Anonymous.ROLE_ANONYMOUS_VALUE)

    @JvmStatic
    fun isDefaultAuthority(authority: String): Boolean {
        return DEFAULT_GRANTED_AUTHORITY.authority == authority
    }

    @JvmStatic
    fun isDefaultAuthority(authority: GrantedAuthority): Boolean {
        return DEFAULT_GRANTED_AUTHORITY == authority
    }

    @JvmStatic
    fun defaultAuthority(): Collection<GrantedAuthority> {
        return setOf(DEFAULT_GRANTED_AUTHORITY)
    }

    @JvmStatic
    fun addDefaultAuthority(
            vararg authorities: GrantedAuthority
    ): Collection<GrantedAuthority> {
        val objects = HashSet(listOf(*authorities))
        objects.add(DEFAULT_GRANTED_AUTHORITY)
        return objects
    }

    @JvmStatic
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

    @JvmStatic
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
