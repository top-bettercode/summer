package top.bettercode.summer.data.jpa.test

import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import java.util.*

@Configuration
class JpaAuditingConfig : AuditorAware<String> {
    var i = 0

    // 返回当前用户的信息
    override fun getCurrentAuditor(): Optional<String> {
        return Optional.of("peter${i++}")
    }
}
