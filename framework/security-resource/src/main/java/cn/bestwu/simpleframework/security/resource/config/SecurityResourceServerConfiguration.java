package cn.bestwu.simpleframework.security.resource.config;

import cn.bestwu.simpleframework.security.resource.IResourceService;
import cn.bestwu.simpleframework.security.resource.URLFilterInvocationSecurityMetadataSource;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@ConditionalOnWebApplication
public class SecurityResourceServerConfiguration {

  private final Logger log = LoggerFactory.getLogger(SecurityResourceServerConfiguration.class);

  @Bean
  public URLFilterInvocationSecurityMetadataSource securityMetadataSource(
      IResourceService apiService,
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      @Value("${security.url-filter.ignored:}") String[] ignored,
      @Value("${security.ignore-logs:true}") boolean ignoreLogs) {
    return new URLFilterInvocationSecurityMetadataSource(apiService,
        requestMappingHandlerMapping, ignored, ignoreLogs);
  }

  @ConditionalOnMissingBean
  @Bean
  public AccessDecisionManager accessDecisionManager() {
    return new AccessDecisionManager() {
      @Override
      public void decide(Authentication authentication, Object object,
          Collection<ConfigAttribute> configAttributes) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (log.isDebugEnabled()) {
          log.debug("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
              StringUtils.collectionToCommaDelimitedString(authorities),
              StringUtils.collectionToCommaDelimitedString(configAttributes.stream().map(
                  (Function<ConfigAttribute, Object>) ConfigAttribute::getAttribute).collect(
                  Collectors.toList())));
        }

        for (ConfigAttribute configAttribute : configAttributes) {//需要的权限，有任意其中一个即可
          if (contains(authorities, configAttribute)) {
            return;
          }
        }
        log.info("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
            StringUtils.collectionToCommaDelimitedString(authorities),
            StringUtils.collectionToCommaDelimitedString(configAttributes.stream().map(
                (Function<ConfigAttribute, Object>) ConfigAttribute::getAttribute).collect(
                Collectors.toList())));
        throw new AccessDeniedException("无权访问");
      }

      private boolean contains(Collection<? extends GrantedAuthority> authorities,
          ConfigAttribute attribute) {
        String attributeAttribute = attribute.getAttribute();
        for (GrantedAuthority authority : authorities) {
          if (attributeAttribute.equals(authority.getAuthority())) {
            return true;
          }
        }
        return false;
      }

      @Override
      public boolean supports(ConfigAttribute attribute) {
        return true;
      }

      @Override
      public boolean supports(Class<?> clazz) {
        return true;
      }

    };
  }

}