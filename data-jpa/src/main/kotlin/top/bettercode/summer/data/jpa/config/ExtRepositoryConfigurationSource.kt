package top.bettercode.summer.data.jpa.config

/**
 *
 * @author Peter Wu
 */
data class ExtRepositoryConfigurationSource(
        val configClass: Class<*>, val annotation: EnableJpaExtRepositories
)