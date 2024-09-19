package top.bettercode.summer.env

import org.springframework.boot.env.OriginTrackedMapPropertySource
import org.springframework.boot.origin.OriginTrackedValue
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.stereotype.Component

/**
 * Entry point for making local (but volatile) changes to the [Environment] of a running
 * application. Allows properties to be added and values changed, simply by adding them to a
 * high-priority property source in the existing Environment.
 */
@Component
@ManagedResource
class EnvironmentManager(val environment: ConfigurableEnvironment) :
    ApplicationEventPublisherAware {
    private val map: MutableMap<String, Any?> by lazy {
        val sources = environment.propertySources
        if (sources.contains(MANAGER_PROPERTY_SOURCE)) {
            @Suppress("UNCHECKED_CAST") val map = sources[MANAGER_PROPERTY_SOURCE]!!
                .source as MutableMap<String, Any?>
            map
        } else {
            LinkedHashMap()
        }
    }
    private var publisher: ApplicationEventPublisher? = null

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
    fun setProperties(propertySources: List<PropertySource<*>>): Map<String, String?> {
        if (!environment.propertySources.contains(MANAGER_PROPERTY_SOURCE)) {
            synchronized(map) {
                if (!environment.propertySources.contains(MANAGER_PROPERTY_SOURCE)) {
                    val source = MapPropertySource(MANAGER_PROPERTY_SOURCE, map)
                    environment.propertySources.addFirst(source)
                }
            }
        }
        val changed: MutableMap<String, String?> = HashMap()
        propertySources.forEach { propertySource ->
            val mapPropertySource = propertySource as OriginTrackedMapPropertySource
            for ((name, value) in mapPropertySource.source) {
                val `val` = (value as OriginTrackedValue?)?.value?.toString()
                val change = `val` != environment.getProperty(name)
                if (change) {
                    map[name] = `val`
                    changed[name] = `val`
                }
            }
        }
        if (changed.isNotEmpty() && publisher != null) {
            publish(EnvironmentChangeEvent(publisher!!, changed.keys))
        }
        return changed
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
