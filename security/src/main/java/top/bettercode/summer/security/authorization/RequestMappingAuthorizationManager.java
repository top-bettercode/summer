package top.bettercode.summer.security.authorization;

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
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher.MatchResult;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import top.bettercode.summer.security.IResource;
import top.bettercode.summer.security.IResourceService;
import top.bettercode.summer.security.authorize.ConfigAuthority;
import top.bettercode.summer.security.authorize.DefaultAuthority;
import top.bettercode.summer.security.config.ApiSecurityProperties;
import top.bettercode.summer.web.AnnotatedUtils;

/**
 * 自定义权限过滤
 *
 * @author Peter Wu
 */
public class RequestMappingAuthorizationManager implements
    AuthorizationManager<RequestAuthorizationContext> {

  private static final AuthorizationDecision DENY = new AuthorizationDecision(false);
  private static final AuthorizationDecision ALLOW = new AuthorizationDecision(true);
  private final Logger log = LoggerFactory.getLogger(RequestMappingAuthorizationManager.class);
  private final Map<RequestMatcher, Set<String>> defaultConfigAuthorities = new HashMap<>();
  private Map<RequestMatcher, Set<String>> configAuthorities = new HashMap<>();
  private final IResourceService securityService;

  // ~ Constructors
  // ===================================================================================================
  public RequestMappingAuthorizationManager(
      IResourceService securityService,
      RequestMappingHandlerMapping handlerMapping,
      ApiSecurityProperties securityProperties) {
    this.securityService = securityService;

    handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
      for (PathPattern pathPattern : Objects.requireNonNull(mappingInfo.getPathPatternsCondition())
          .getPatterns()) {
        String pattern = pathPattern.getPatternString();
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        Set<String> authorities = new HashSet<>();
        if (securityProperties.ignored(pattern)) {
          if (securityService.supportsAnonymous()) {
            authorities.add(DefaultAuthority.ROLE_ANONYMOUS_VALUE);
          } else {
            authorities.add(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE);
          }
        } else {
          Set<ConfigAuthority> authoritySet = AnnotatedUtils
              .getAnnotations(handlerMethod, ConfigAuthority.class);
          if (authoritySet.isEmpty()) {
            authorities.add(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE);
          } else {
            for (ConfigAuthority authority : authoritySet) {
              for (String s : authority.value()) {
                s = s.trim();
                Assert.hasText(s, "权限标记不能为空");
                if (DefaultAuthority.ROLE_ANONYMOUS_VALUE.equals(s)) {
                  if (securityService.supportsAnonymous()) {
                    authorities.add(DefaultAuthority.ROLE_ANONYMOUS_VALUE);
                  } else {
                    authorities.add(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE);
                  }
                } else {
                  authorities.add(s);
                }
              }
            }
          }
        }

        if (methods.isEmpty()) {
          defaultConfigAuthorities.put(new AntPathRequestMatcher(pattern), authorities);
        } else {
          for (RequestMethod requestMethod : methods) {
            defaultConfigAuthorities.put(new AntPathRequestMatcher(pattern, requestMethod.name()),
                authorities);
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
    if (this.log.isTraceEnabled()) {
      this.log.trace("Authorizing {}", request);
    }
    Collection<? extends GrantedAuthority> userAuthorities = authentication.get().getAuthorities();

    for (Entry<RequestMatcher, Set<String>> requestMatcher : configAuthorities.entrySet()) {
      RequestMatcher matcher = requestMatcher.getKey();
      MatchResult matchResult = matcher.matcher(request);
      if (matchResult.isMatch()) {
        Set<String> authorities = requestMatcher.getValue();
        if (log.isDebugEnabled()) {
          log.debug("权限检查，当前用户权限：{}，当前资源需要以下权限之一：{}",
              StringUtils.collectionToCommaDelimitedString(userAuthorities),
              authorities);
        }
        if (authorities.contains(DefaultAuthority.ROLE_ANONYMOUS_VALUE)) {
          return ALLOW;
        }
        boolean granted = isGranted(authentication.get(), authorities);
        return new AuthorizationDecision(granted);
      }
    }
    if (this.log.isTraceEnabled()) {
      this.log.trace("allow request since did not find matching RequestMatcher");
    }

    return ALLOW;
  }

  private boolean isGranted(Authentication authentication, Set<String> authorities) {
    return authentication != null && authentication.isAuthenticated() && isAuthorized(
        authentication, authorities);
  }

  private boolean isAuthorized(Authentication authentication, Set<String> authorities) {
    for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
      for (String authority : authorities) {
        if (authority.equals(grantedAuthority.getAuthority())) {
          return true;
        }
      }
    }
    return false;
  }


  public void bindAuthorizationManager() {
    configAuthorities = new HashMap<>(defaultConfigAuthorities);

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
                Assert.isNull(configAuthorities.get(new AntPathRequestMatcher(u)),
                    "\"" + u + "\"对应RequestMapping不包含请求方法描述，请使用通用路径\"" + u
                        + "\"配置权限");
                Set<String> authorities = configAuthorities.computeIfAbsent(
                    new AntPathRequestMatcher(u, m), k -> new HashSet<>());
                authorities.add(configAttribute);
              }
            } else {
              Set<String> authorities = configAuthorities.computeIfAbsent(
                  new AntPathRequestMatcher(u), k -> new HashSet<>());
              authorities.add(configAttribute);
            }
          }
        } else {
          for (String u : api.split("\\|")) {
            Set<String> authorities = configAuthorities.computeIfAbsent(
                new AntPathRequestMatcher(u), k -> new HashSet<>());
            authorities.add(configAttribute);
          }
        }
      }
    }
  }

}