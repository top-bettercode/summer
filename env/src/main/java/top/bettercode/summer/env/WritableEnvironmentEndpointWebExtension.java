package top.bettercode.summer.env;

import java.util.Collections;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.env.EnvironmentEndpointWebExtension;

/**
 * MVC endpoint for the {@link EnvironmentManager}, providing a POST to /env as a simple way to
 * change the Environment.
 */
@EndpointWebExtension(endpoint = WritableEnvironmentEndpoint.class)
public class WritableEnvironmentEndpointWebExtension extends EnvironmentEndpointWebExtension {

  private EnvironmentManager environment;

  public WritableEnvironmentEndpointWebExtension(WritableEnvironmentEndpoint endpoint,
      EnvironmentManager environment) {
    super(endpoint);
    this.environment = environment;
  }

  @WriteOperation
  public Object write(String name, String value) {
    this.environment.setProperty(name, value);
    return Collections.singletonMap(name, value);
  }

  @DeleteOperation
  public Map<String, Object> reset() {
    return this.environment.reset();
  }

  public void setEnvironmentManager(EnvironmentManager environment) {
    this.environment = environment;
  }

}
