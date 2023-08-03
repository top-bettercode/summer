package top.bettercode.summer.env

import org.springframework.context.ApplicationEvent

/**
 * Event published to signal a change in the [Environment].
 */
class EnvironmentChangeEvent(
        context: Any,
        /**
         * @return The keys.
         */
        val keys: Set<String>
) : ApplicationEvent(context) {

    constructor(keys: Set<String>) : this(keys, keys)

}
