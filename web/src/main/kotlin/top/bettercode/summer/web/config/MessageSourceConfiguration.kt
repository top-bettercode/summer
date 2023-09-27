package top.bettercode.summer.web.config

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.*
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration
import org.springframework.boot.autoconfigure.context.MessageSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.Ordered
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.util.ConcurrentReferenceHashMap

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(value = [MessageSource::class], search = SearchStrategy.CURRENT)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(MessageSourceAutoConfiguration::class)
@EnableConfigurationProperties
class MessageSourceConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "spring.messages")
    fun messageSourceProperties(): MessageSourceProperties {
        return MessageSourceProperties()
    }

    @Bean
    fun messageSource(applicationContext: ApplicationContext,
                      messageSourceProperties: MessageSourceProperties): MessageSource {
        var basename = messageSourceProperties.basename
        val messageNames = basename.trim().split(",").toMutableList()
        val defaultMessagesName = "messages"
        val classLoader = applicationContext.classLoader!!
        if (messageNames.contains(defaultMessagesName) && ResourceBundleCondition.getResources(classLoader, defaultMessagesName).isEmpty()) {
            messageNames.remove(defaultMessagesName)
        }
        messageNames.add(BASE_MESSAGES)
        if (!messageNames.contains(CORE_MESSAGES)) {
            val resources = ResourceBundleCondition.getResources(classLoader, CORE_MESSAGES)
            if (resources.isNotEmpty() && resources[0]!!.exists()) {
                messageNames.add(CORE_MESSAGES)
            }
        }
        basename = messageNames.joinToString(",")
        messageSourceProperties.basename = basename
        val messageSource = ResourceBundleMessageSource()
        if (messageNames.isNotEmpty()) {
            messageSource.setBasenames(*messageNames.toTypedArray<String>())
        }
        if (messageSourceProperties.encoding != null) {
            messageSource.setDefaultEncoding(messageSourceProperties.encoding.name())
        }
        messageSource.setFallbackToSystemLocale(messageSourceProperties.isFallbackToSystemLocale)
        val cacheDuration = messageSourceProperties.cacheDuration
        if (cacheDuration != null) {
            messageSource.setCacheMillis(cacheDuration.toMillis())
        }
        messageSource.setAlwaysUseMessageFormat(messageSourceProperties.isAlwaysUseMessageFormat)
        messageSource.setUseCodeAsDefaultMessage(messageSourceProperties.isUseCodeAsDefaultMessage)
        return messageSource
    }

    protected class ResourceBundleCondition : SpringBootCondition() {
        override fun getMatchOutcome(context: ConditionContext,
                                     metadata: AnnotatedTypeMetadata): ConditionOutcome {
            val basename = context.environment
                    .getProperty("spring.messages.basename", "messages")
            var outcome = cache[basename]
            if (outcome == null) {
                outcome = getMatchOutcomeForBasename(context, basename)
                cache[basename] = outcome
            }
            return outcome
        }

        private fun getMatchOutcomeForBasename(context: ConditionContext,
                                               basename: String): ConditionOutcome {
            val message = ConditionMessage.forCondition("ResourceBundle")
            val messageNames = basename.trim().split(",").toMutableList()
            for (name in messageNames) {
                for (resource in getResources(context.classLoader!!, name)) {
                    if (resource!!.exists()) {
                        return ConditionOutcome
                                .match(message.found("bundle").items(resource))
                    }
                }
            }
            return ConditionOutcome.noMatch(
                    message.didNotFind("bundle with basename $basename").atAll())
        }

        companion object {
            private val cache = ConcurrentReferenceHashMap<String, ConditionOutcome>()
            fun getResources(classLoader: ClassLoader, name: String): Array<Resource?> {
                val target = name.replace('.', '/')
                return try {
                    PathMatchingResourcePatternResolver(classLoader)
                            .getResources("classpath*:$target.properties")
                } catch (ex: Exception) {
                    NO_RESOURCES
                }
            }
        }
    }

    companion object {
        const val BASE_MESSAGES = "base-messages"
        const val CORE_MESSAGES = "core-messages"
        private val NO_RESOURCES = arrayOfNulls<Resource>(0)
    }
}
