package cn.bestwu.config;

import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.core.env.Environment;

/**
 * An extension of the standard {@link EnvironmentEndpoint} that allows to modify the
 * environment at runtime.
 *
 */
public class WritableEnvironmentEndpoint extends EnvironmentEndpoint {

	public WritableEnvironmentEndpoint(Environment environment) {
		super(environment);
	}

}
