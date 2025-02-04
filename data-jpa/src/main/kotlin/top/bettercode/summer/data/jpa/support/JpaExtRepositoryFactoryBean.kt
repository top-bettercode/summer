package top.bettercode.summer.data.jpa.support

import org.apache.ibatis.session.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.support.JpaExtRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.JpaExtRepository
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import java.io.Serializable
import javax.persistence.EntityManager

/**
 * [JpaRepositoryFactoryBean] to return a custom repository base class.
 *
 * @author Peter Wu
 */
class JpaExtRepositoryFactoryBean<T : JpaExtRepository<Any?, Serializable?>?>(repositoryInterface: Class<out T>) :
    JpaRepositoryFactoryBean<T, Any?, Serializable?>(repositoryInterface) {
    lateinit var jpaExtProperties: JpaExtProperties
    lateinit var auditorAware: AuditorAware<*>
    lateinit var mybatisConfiguration: Configuration

    /*
   * (non-Javadoc)
   *
   * @see org.springframework.data.jpa.repository.support.
   * GenericJpaRepositoryFactoryBean#getFactory()
   */
    override fun createRepositoryFactory(em: EntityManager): RepositoryFactorySupport {
        Assert.notNull(mybatisConfiguration, "mybatisConfiguration must not be null")
        Assert.notNull(jpaExtProperties, "jpaExtProperties must not be null")
        Assert.notNull(auditorAware, "auditorAware must not be null")
        return JpaExtRepositoryFactory(em, mybatisConfiguration, jpaExtProperties, auditorAware)
    }
}
