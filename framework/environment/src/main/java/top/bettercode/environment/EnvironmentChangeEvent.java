package top.bettercode.environment;

import java.util.Set;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;

/**
 * Event published to signal a change in the {@link Environment}.
 *
 */
@SuppressWarnings("serial")
public class EnvironmentChangeEvent extends ApplicationEvent {

	private Set<String> keys;

	public EnvironmentChangeEvent(Set<String> keys) {
		// Backwards compatible constructor with less utility (practically no use at all)
		this(keys, keys);
	}

	public EnvironmentChangeEvent(Object context, Set<String> keys) {
		super(context);
		this.keys = keys;
	}

	/**
	 * @return The keys.
	 */
	public Set<String> getKeys() {
		return this.keys;
	}

}
