package top.bettercode.summer.security.authorization;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;
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
  private final Map<AntPathRequestMatcher, Set<String>> defaultConfigAuthorities = new HashMap<>();
  private Map<AntPathRequestMatcher, Set<String>> configAuthorities = new HashMap<>();
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
          authorities.add(DefaultAuthority.ROLE_ANONYMOUS_VALUE);
        } else {
          Set<ConfigAuthority> authoritySet = AnnotatedUtils
              .getAnnotations(handlerMethod, ConfigAuthority.class);
          if (!authoritySet.isEmpty()) {
            for (ConfigAuthority authority : authoritySet) {
              for (String s : authority.value()) {
                Assert.hasText(s, "权限标记不能为空");
                authorities.add(s);
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

    List<Entry<AntPathRequestMatcher, Set<String>>> matchers = configAuthorities.entrySet().stream()
        .filter(entry -> entry.getKey().matcher(request).isMatch()).collect(Collectors.toList());
    if (matchers.isEmpty()) {
      if (this.log.isTraceEnabled()) {
        this.log.trace("allow request since did not find matching RequestMatcher");
      }
      return ALLOW;
    }
    Comparator<String> comparator = new AntPathMatcher().getPatternComparator(
        getRequestPath(request));
    matchers.stream()
        .sorted((o1, o2) -> comparator.compare(o1.getKey().getPattern(), o2.getKey().getPattern()));

    Entry<AntPathRequestMatcher, Set<String>> bestMatch = matchers.get(0);
    if (matchers.size() > 1) {
      Entry<AntPathRequestMatcher, Set<String>> secondBestMatch = matchers.get(1);
      String pattern1 = bestMatch.getKey().getPattern();
      String pattern2 = secondBestMatch.getKey().getPattern();
      if (comparator.compare(pattern1, pattern2) == 0) {
        throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
            request.getRequestURL() + "': {" + pattern1 + ", " + pattern2 + "}");
      }
    }

    Set<String> authorities = new HashSet<>(bestMatch.getValue());
    if (authorities.isEmpty()) {
      authorities = Collections.singleton(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE);
    }
    if (authorities.contains(DefaultAuthority.ROLE_ANONYMOUS_VALUE)) {
      if (securityService.supportsAnonymous()) {
        if (log.isDebugEnabled()) {
          log.debug("权限检查，当前用户权限：{}，当前资源({})需要以下权限之一：{}",
              StringUtils.collectionToCommaDelimitedString(userAuthorities),
              request.getServletPath(),
              authorities);
        }
        return ALLOW;
      } else {
        authorities.remove(DefaultAuthority.ROLE_ANONYMOUS_VALUE);
        authorities.add(DefaultAuthority.DEFAULT_AUTHENTICATED_VALUE);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("权限检查，当前用户权限：{}，当前资源({})需要以下权限之一：{}",
          StringUtils.collectionToCommaDelimitedString(userAuthorities),
          request.getServletPath(),
          authorities);
    }
    boolean granted = isGranted(authentication.get(), authorities);
    return new AuthorizationDecision(granted);
  }

  private String getRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
      url += request.getPathInfo();
    }

    return url;
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
            AntPathRequestMatcher urlMatcher = new AntPathRequestMatcher(u);
            if (StringUtils.hasText(method)) {
              Assert.isNull(configAuthorities.get(urlMatcher),
                  "\"" + u + "\"对应RequestMapping不包含请求方法描述，请使用通用路径\"" + u
                      + "\"配置权限");
              for (String m : method.split("\\|")) {
                Set<String> authorities = configAuthorities.computeIfAbsent(
                    new AntPathRequestMatcher(u, m), k -> new HashSet<>());
                authorities.add(configAttribute);
              }
            } else {
              Set<String> authorities = configAuthorities.computeIfAbsent(
                  urlMatcher, k -> new HashSet<>());
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