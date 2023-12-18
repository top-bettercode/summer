package top.bettercode.summer.env

import org.springframework.boot.actuate.endpoint.SanitizingFunction
import org.springframework.boot.actuate.endpoint.Show
import org.springframework.boot.actuate.env.EnvironmentEndpoint
import org.springframework.core.env.Environment

/**
 * An extension of the standard [EnvironmentEndpoint] that allows to modify the environment at
 * runtime.
 */
class WritableEnvironmentEndpoint(environment: Environment,
                                  sanitizingFunctions: Iterable<SanitizingFunction>,
                                  showValues: Show) : EnvironmentEndpoint(environment, sanitizingFunctions, showValues)