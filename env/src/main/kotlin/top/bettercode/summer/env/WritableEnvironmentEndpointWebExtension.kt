package top.bettercode.summer.env

import org.springframework.boot.actuate.endpoint.Show
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension
import org.springframework.boot.actuate.env.EnvironmentEndpoint
import org.springframework.boot.actuate.env.EnvironmentEndpointWebExtension
import java.util.*

/**
 * MVC endpoint for the [EnvironmentManager], providing a POST to /env as a simple way to
 * change the Environment.
 */
@EndpointWebExtension(endpoint = WritableEnvironmentEndpoint::class)
class WritableEnvironmentEndpointWebExtension(
        delegate: EnvironmentEndpoint,
        showValues: Show, roles: Set<String>,
        private var environment: EnvironmentManager) : EnvironmentEndpointWebExtension(delegate, showValues, roles) {
    @WriteOperation
    fun write(name: String, value: String?): Any {
        environment.setProperty(name, value)
        return Collections.singletonMap(name, value)
    }

    @DeleteOperation
    fun reset(): Map<String, Any?> {
        return environment.reset()
    }

    fun setEnvironmentManager(environment: EnvironmentManager) {
        this.environment = environment
    }
}
