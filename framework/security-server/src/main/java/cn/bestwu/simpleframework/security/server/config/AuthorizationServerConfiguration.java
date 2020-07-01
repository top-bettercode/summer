package cn.bestwu.simpleframework.security.server.config;

import cn.bestwu.simpleframework.security.ClientAuthorize;
import cn.bestwu.simpleframework.security.server.AuthorizationServerHttpSecurityConfigurerAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.server.core.AnnotationMappingDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configuration.ClientDetailsServiceConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * OAuth2 服务器自动配置
 *
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
@ConditionalOnClass(OAuth2Exception.class)
@Order(-1)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@Configuration
@ConditionalOnWebApplication
@Import({ClientDetailsServiceConfiguration.class, AuthorizationServerEndpointsConfiguration.class})
public class AuthorizationServerConfiguration extends AuthorizationServerSecurityConfiguration {

  private List<AuthorizationServerHttpSecurityConfigurerAdapter> configurers = Collections
      .emptyList();

  @Value("${security.cors.enable:false}")
  private boolean enableCors;
  private final ApplicationContext applicationContext;

  public AuthorizationServerConfiguration(
      ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Autowired(required = false)
  public void setConfigurers(List<AuthorizationServerHttpSecurityConfigurerAdapter> configurers) {
    this.configurers = configurers;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    if (enableCors) {
      http.cors();
    }
    Map<String, Object> beansWithAnnotation = applicationContext
        .getBeansWithAnnotation(Controller.class);
    beansWithAnnotation.putAll(applicationContext.getBeansWithAnnotation(FrameworkEndpoint.class));
    beansWithAnnotation.putAll(applicationContext.getBeansWithAnnotation(ClientAuthorize.class));
    final MultiValueMap<HttpMethod, String> cachedAntMatchers = new LinkedMultiValueMap<>();
    for (Object o : beansWithAnnotation.values()) {
      final AnnotationMappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(
          RequestMapping.class);

      ReflectionUtils.doWithMethods(o.getClass(), method -> {

        ClientAuthorize clientAuthorize = AnnotationUtils
            .findAnnotation(method, ClientAuthorize.class);
        if (clientAuthorize != null) {
          RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
          if (mapping != null) {
            String url = DISCOVERER.getMapping(method);
            RequestMethod[] methods = mapping.method();
            if (methods.length == 0) {
              cachedAntMatchers.add(null, url);
            } else {
              for (RequestMethod requestMethod : methods) {
                cachedAntMatchers
                    .add(HttpMethod.valueOf(requestMethod.name()), url);
              }
            }
          } else if (clientAuthorize.value().length > 0) {
            for (String url : clientAuthorize.value()) {
              cachedAntMatchers.add(clientAuthorize.method(), url);
            }
          }
        }
      }, USER_METHODS);
    }

    for (HttpMethod method : cachedAntMatchers.keySet()) {
      String[] antPatterns = cachedAntMatchers.get(method).toArray(new String[0]);

      http
          .requestMatchers()
          .antMatchers(method, antPatterns).and()
          .authorizeRequests()
          .antMatchers(method, antPatterns).fullyAuthenticated();
    }

    for (AuthorizationServerHttpSecurityConfigurerAdapter configurer : configurers) {
      configurer.configure(http);
    }
  }

  /**
   * 排除代理方法
   */
  public static final ReflectionUtils.MethodFilter USER_METHODS = method -> !method.isSynthetic() &&
      !method.isBridge() &&
      !ReflectionUtils.isObjectMethod(method) &&
      !ClassUtils.isCglibProxyClass(method.getDeclaringClass()) &&
      !ReflectionUtils.isCglibRenamedMethod(method);
}
