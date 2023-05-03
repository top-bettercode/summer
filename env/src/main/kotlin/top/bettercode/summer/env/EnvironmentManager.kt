package top.bettercode.summer.env

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.stereotype.Component
import java.util.*

/**
 * Entry point for making local (but volatile) changes to the [Environment] of a running
 * application. Allows properties to be added and values changed, simply by adding them to a
 * high-priority property source in the existing Environment.
 */
@Component
@ManagedResource
class EnvironmentManager(private val environment: ConfigurableEnvironment) : ApplicationEventPublisherAware {
    private val map: MutableMap<String, Any?>
    private var publisher: ApplicationEventPublisher? = null

    init {
        val sources = environment.propertySources
        if (sources.contains(MANAGER_PROPERTY_SOURCE)) {
            @Suppress("UNCHECKED_CAST") val map = Objects.requireNonNull(
                    sources[MANAGER_PROPERTY_SOURCE])
                    .source as MutableMap<String, Any?>
            this.map = map
        } else {
            this.map = LinkedHashMap()
        }
    }

    override fun setApplicationEventPublisher(publisher: ApplicationEventPublisher) {
        this.publisher = publisher
    }

    @ManagedOperation
    fun reset(): Map<String, Any?> {
        val result: Map<String, Any?> = LinkedHashMap(map)
        if (map.isNotEmpty()) {
            map.clear()
            if (publisher != null) {
                publish(EnvironmentChangeEvent(publisher!!, result.keys))
            }
        }
        return result
    }

    @ManagedOperation
    fun setProperty(name: String, value: String?): Boolean {
        if (!environment.propertySources.contains(MANAGER_PROPERTY_SOURCE)) {
            synchronized(map) {
                if (!environment.propertySources.contains(MANAGER_PROPERTY_SOURCE)) {
                    val source = MapPropertySource(MANAGER_PROPERTY_SOURCE, map)
                    environment.propertySources.addFirst(source)
                }
            }
        }
        if (value != environment.getProperty(name)) {
            map[name] = value
            if (publisher != null) {
                publish(EnvironmentChangeEvent(publisher!!, setOf(name)))
            }
            return true
        }
        return false
    }

    @ManagedOperation
    fun getProperty(name: String): Any? {
        return environment.getProperty(name)
    }

    private fun publish(environmentChangeEvent: EnvironmentChangeEvent) {
        if (publisher != null) {
            publisher!!.publishEvent(environmentChangeEvent)
        }
    }

    companion object {
        private const val MANAGER_PROPERTY_SOURCE = "manager"
    }
}
