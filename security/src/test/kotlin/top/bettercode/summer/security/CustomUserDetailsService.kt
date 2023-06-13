package top.bettercode.summer.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import top.bettercode.summer.security.authorize.DefaultAuthority
import top.bettercode.summer.security.support.IllegalUserException
import top.bettercode.summer.security.userdetails.AdditionalUserDetails

/**
 * 自定义UserDetailsService
 *
 * @author Peter Wu
 */
@Service
class CustomUserDetailsService : UserDetailsService {
    /**
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 未找到用户
     */
    override fun loadUserByUsername(username: String): UserDetails {
        if ("disableUsername" == username) {
            throw IllegalUserException("帐户已禁用")
        }
        val additionalUserDetails = AdditionalUserDetails(username,
                DigestUtils.md5DigestAsHex("123456".toByteArray()),
                getAuthorities(username))
        additionalUserDetails.put("addkey", "addvalue")
        return additionalUserDetails
    }

    fun getAuthorities(username: String): Collection<GrantedAuthority?> {
        return if (username == "root") {
            DefaultAuthority.addDefaultAuthority(SimpleGrantedAuthority("cust"))
        } else DefaultAuthority.defaultAuthority()
    }
}