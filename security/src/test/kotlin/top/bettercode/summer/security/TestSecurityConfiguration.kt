package top.bettercode.summer.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import top.bettercode.summer.security.token.IRevokeTokenService
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
class TestSecurityConfiguration {
    //  @Bean
    //  public RedisApiAuthorizationService redisApiAuthorizationService(
    //      RedisConnectionFactory redisConnectionFactory) {
    //    return new RedisApiAuthorizationService(redisConnectionFactory, "test");
    //  }
    //  @Bean
    //  public JdbcApiAuthorizationService jdbcApiAuthorizationService(
    //      DataSource dataSource) {
    //    return new JdbcApiAuthorizationService(dataSource);
    //  }
    @Bean
    fun revokeTokenService(): IRevokeTokenService {
        return IRevokeTokenService { principal ->
            System.err
                    .println(valueOf(principal, true))
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return object : PasswordEncoder {
            override fun encode(rawPassword: CharSequence): String {
                return rawPassword.toString()
            }

            override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
                return rawPassword.toString() == encodedPassword
            }
        }
    }
}