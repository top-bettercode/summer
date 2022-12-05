package top.bettercode.summer.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorityAuthorizationDecision;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher.MatchResult;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.web.AnnotatedUtils;

/**
 * 自定义权限过滤
 *
 * @author Peter Wu
 */
public class RequestMappingAuthorizationManager implements
    AuthorizationManager<RequestAuthorizationContext> {

  private static final AntPathRequestMatcher actuatorPathMatcher = new AntPathRequestMatcher(
      "/actuator/**");
  private static final AuthorizationDecision DENY = new AuthorizationDecision(false);
  private static final AuthorizationDecision ALLOW = new AuthorizationDecision(true);
  private final Logger log = LoggerFactory.getLogger(RequestMappingAuthorizationManager.class);
  private final Map<RequestMatcher, Set<String>> defaultConfigAttributes = new HashMap<>();
  private final List<RequestMatcherEntry<AuthorityAuthorizationManagerExt>> mappings = new ArrayList<>();
  private final IResourceService securityService;
  private final ApiSecurityProperties securityProperties;

  // ~ Constructors
  // ===================================================================================================
  public RequestMappingAuthorizationManager(
      IResourceService securityService,
      RequestMappingHandlerMapping handlerMapping,
      ApiSecurityProperties securityProperties) {
    this.securityService = securityService;
    this.securityProperties = securityProperties;

    handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
      for (PathPattern pathPattern : Objects.requireNonNull(mappingInfo.getPathPatternsCondition())
          .getPatterns()) {
        String pattern = pathPattern.getPatternString();
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        Set<String> configAttributes = new HashSet<>();
        if (securityProperties.ignored(pattern)) {
          configAttributes.add(DefaultAuthority.ROLE_ANONYMOUS.getAttribute());
        } else {
          Set<ConfigAuthority> authoritySet = AnnotatedUtils
              .getAnnotations(handlerMethod, ConfigAuthority.class);
          if (authoritySet.isEmpty()) {
            configAttributes.add(securityProperties.getDefaultAuthority());
          } else {
            for (ConfigAuthority authority : authoritySet) {
              for (String s : authority.value()) {
                Assert.hasText(s, "权限标记不能为空");
                configAttributes.add(s.trim());
              }
            }
          }
        }

        if (methods.isEmpty()) {
          defaultConfigAttributes.put(new AntPathRequestMatcher(pattern), configAttributes);
        } else {
          for (RequestMethod requestMethod : methods) {
            defaultConfigAttributes.put(new AntPathRequestMatcher(pattern, requestMethod.name()),
                configAttributes);
          }
        }
      }
    });
    bindAuthorizationManager();
  }

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    HttpServletRequest request = requestAuthorizationContext.getRequest();
    if (actuatorPathMatcher.matches(request)) {
      return ALLOW;
    }
    if (this.log.isTraceEnabled()) {
      this.log.trace("Authorizing {}", request);
    }
    Collection<? extends GrantedAuthority> userAuthorities = authentication.get().getAuthorities();

    for (RequestMatcherEntry<AuthorityAuthorizationManagerExt> mapping : this.mappings) {
      RequestMatcher matcher = mapping.getRequestMatcher();
      MatchResult matchResult = matcher.matcher(request);
      if (matchResult.isMatch()) {
        AuthorityAuthorizationManagerExt entry = mapping.getEntry();
        Set<String> authorities = entry.getAuthorities();

        AuthorizationManager<RequestAuthorizationContext> manager;
        if (authorities.contains(DefaultAuthority.ROLE_ANONYMOUS.getAttribute())) {
          if (securityService.supportsAnonymous()) {
            return new AuthorityAuthorizationDecision(true,
                AuthorityUtils.createAuthorityList(authorities.toArray(new String[0])));
          } else {
            authorities.remove(DefaultAuthority.ROLE_ANONYMOUS);
            authorities.add(securityProperties.getDefaultAuthority());
            manager = AuthorityAuthorizationManager.hasAnyAuthority(
                authorities.toArray(new String[0]));
            entry.setManager(manager);
          }
        } else {
          manager = entry.getManager();
        }

        if (log.isDebugEnabled()) {
          log.debug("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
              StringUtils.collectionToCommaDelimitedString(userAuthorities),
              StringUtils.collectionToCommaDelimitedString(authorities));
        }

        if (this.log.isTraceEnabled()) {
          this.log.trace("Checking authorization on {} using {}", request, manager);
        }
        return manager.check(authentication,
            new RequestAuthorizationContext(request, matchResult.getVariables()));
      }
    }
    if (this.log.isTraceEnabled()) {
      this.log.trace("Denying request since did not find matching RequestMatcher");
    }
    return DENY;
  }

  protected void bindAuthorizationManager() {
    HashMap<RequestMatcher, Set<String>> requestMatcherConfigAttributes = new HashMap<>(
        defaultConfigAttributes);

    List<? extends IResource> allResources = securityService.findAllResources();
    for (IResource resource : allResources) {
      String ress = resource.getRess();
      String configAttribute = resource.getMark().trim();
      Assert.hasText(configAttribute, "权限标记不能为空");
      for (String api : ress.split(",")) {
        if (api.contains(":")) {
          String[] methodUrl = api.split(":");
          String method = methodUrl[0].toUpperCase();
          String url = methodUrl[1];
          for (String u : url.split("\\|")) {
            if (StringUtils.hasText(method)) {
              for (String m : method.split("\\|")) {
                Assert.isNull(requestMatcherConfigAttributes.get(new AntPathRequestMatcher(u)),
                    "\"" + u + "\"对应RequestMapping不包含请求方法描述，请使用通用路径\"" + u
                        + "\"配置权限");
                Set<String> authorities = requestMatcherConfigAttributes
                    .computeIfAbsent(new AntPathRequestMatcher(u, m),
                        k -> new HashSet<>());
                authorities.add(configAttribute);
              }
            } else {
              Set<String> authorities = requestMatcherConfigAttributes
                  .computeIfAbsent(new AntPathRequestMatcher(u),
                      k -> new HashSet<>());
              authorities.add(configAttribute);
            }
          }
        } else {
          for (String u : api.split("\\|")) {
            Set<String> authorities = requestMatcherConfigAttributes
                .computeIfAbsent(new AntPathRequestMatcher(u), k -> new HashSet<>());
            authorities.add(configAttribute);
          }
        }
      }
    }

    mappings.clear();
    for (Entry<RequestMatcher, Set<String>> entry : requestMatcherConfigAttributes.entrySet()) {
      String[] authorities = entry.getValue().toArray(new String[0]);
      mappings.add(new RequestMatcherEntry<>(entry.getKey(), new AuthorityAuthorizationManagerExt(
          AuthorityAuthorizationManager.hasAnyAuthority(authorities), entry.getValue())));
    }
  }

}