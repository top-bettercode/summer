package cn.bestwu.simpleframework.web.filter;

import cn.bestwu.simpleframework.config.LogDocAuthProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.HtmlUtils;

/**
 * For internal use with namespace configuration in the case where a user doesn't configure a login
 * page. The configuration code will insert this filter in the chain instead.
 * <p>
 * Will only work if a redirect is used to the login page.
 *
 * @author Luke Taylor
 * @since 2.0
 */
public class LogLoginPageGeneratingFilter extends GenericFilterBean {

  public static final String DEFAULT_LOGIN_PAGE_URL = "/login";
  public static final String ERROR_PARAMETER_NAME = "error";
  public static final String LOGGER_AUTH_KEY =
      LogLoginPageGeneratingFilter.class.getName() + ".auth";
  public static final String TARGET_URL_KEY =
      LogLoginPageGeneratingFilter.class.getName() + ".targetUrl";
  private String loginPageUrl;
  private String logoutSuccessUrl;
  private String failureUrl;
  private String authenticationUrl;
  private String usernameParameter;
  private String pwddParameter;
  private final LogDocAuthProperties logDocAuthProperties;

  private Function<HttpServletRequest, Map<String, String>> resolveHiddenInputs = request -> Collections
      .emptyMap();


  public LogLoginPageGeneratingFilter(
      LogDocAuthProperties logDocAuthProperties) {
    this.logDocAuthProperties = logDocAuthProperties;
    this.loginPageUrl = DEFAULT_LOGIN_PAGE_URL;
    this.authenticationUrl = DEFAULT_LOGIN_PAGE_URL;
    this.logoutSuccessUrl = DEFAULT_LOGIN_PAGE_URL + "?logout";
    this.failureUrl = DEFAULT_LOGIN_PAGE_URL + "?" + ERROR_PARAMETER_NAME;
    this.usernameParameter = "username";
    this.pwddParameter = "password";
  }

  /**
   * Sets a Function used to resolve a Map of the hidden inputs where the key is the name of the
   * input and the value is the value of the input. Typically this is used to resolve the CSRF
   * token.
   *
   * @param resolveHiddenInputs the function to resolve the inputs
   */
  public void setResolveHiddenInputs(
      Function<HttpServletRequest, Map<String, String>> resolveHiddenInputs) {
    Assert.notNull(resolveHiddenInputs, "resolveHiddenInputs cannot be null");
    this.resolveHiddenInputs = resolveHiddenInputs;
  }

  public void setLogoutSuccessUrl(String logoutSuccessUrl) {
    this.logoutSuccessUrl = logoutSuccessUrl;
  }

  public String getLoginPageUrl() {
    return loginPageUrl;
  }

  public void setLoginPageUrl(String loginPageUrl) {
    this.loginPageUrl = loginPageUrl;
  }

  public void setFailureUrl(String failureUrl) {
    this.failureUrl = failureUrl;
  }

  public void setAuthenticationUrl(String authenticationUrl) {
    this.authenticationUrl = authenticationUrl;
  }

  public void setUsernameParameter(String usernameParameter) {
    this.usernameParameter = usernameParameter;
  }

  public void setPwddParameter(String pwddParameter) {
    this.pwddParameter = pwddParameter;
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String uri = request.getServletPath();
    if (logDocAuthProperties.match(uri)) {
      HttpSession session = request.getSession(true);
      if (session.getAttribute(LOGGER_AUTH_KEY) != null) {
        chain.doFilter(request, response);
      } else {
        String queryString = request.getQueryString();
        if (queryString != null) {
          uri += "?" + queryString;
        }
        session.setAttribute(TARGET_URL_KEY, uri);
        sendRedirect(request, response, loginPageUrl);
      }
      return;
    }
    boolean matcheLoginPage = matches(request, loginPageUrl);
    String errorMsg = "Invalid credentials";
    boolean loginError = isErrorPage(request);
    if (matcheLoginPage && "POST".equals(request.getMethod())) {
      String username = request.getParameter(usernameParameter);
      String password = request.getParameter(pwddParameter);
      if (username != null && password != null && username.trim()
          .equals(logDocAuthProperties.getUsername()) && password
          .equals(logDocAuthProperties.getPassword())) {
        HttpSession session = request.getSession(true);
        session.setAttribute(LOGGER_AUTH_KEY, true);
        sendRedirect(request, response, (String) session.getAttribute(TARGET_URL_KEY));
        return;
      }
      errorMsg = "用户名或密码错误";
      loginError = true;
    }

    if ((matcheLoginPage && "GET".equals(request.getMethod())) || loginError || isLogoutSuccess(
        request)) {
      String loginPageHtml = generateLoginPageHtml(request, loginError,
          isLogoutSuccess(request), errorMsg);
      response.setContentType("text/html;charset=UTF-8");
      response.setContentLength(loginPageHtml.getBytes(StandardCharsets.UTF_8).length);
      response.getWriter().write(loginPageHtml);

      return;
    }

    chain.doFilter(request, response);
  }

  public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
      String url) throws IOException {
    String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
    redirectUrl = response.encodeRedirectURL(redirectUrl);

    if (logger.isDebugEnabled()) {
      logger.debug("Redirecting to '" + redirectUrl + "'");
    }

    response.sendRedirect(redirectUrl);
  }

  public static boolean isAbsoluteUrl(String url) {
    if (url == null) {
      return false;
    }
    final Pattern ABSOLUTE_URL = Pattern.compile("\\A[a-z0-9.+-]+://.*",
        Pattern.CASE_INSENSITIVE);

    return ABSOLUTE_URL.matcher(url).matches();
  }

  protected String calculateRedirectUrl(String contextPath, String url) {
    if (!isAbsoluteUrl(url)) {
      return contextPath + url;
    }
    return url;
  }


  private String generateLoginPageHtml(HttpServletRequest request, boolean loginError,
      boolean logoutSuccess, String errorMsg) {
    StringBuilder sb = new StringBuilder();

    sb.append("<!DOCTYPE html>\n"
        + "<html lang=\"en\">\n"
        + "  <head>\n"
        + "    <meta charset=\"utf-8\">\n"
        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n"
        + "    <meta name=\"description\" content=\"\">\n"
        + "    <meta name=\"author\" content=\"\">\n"
        + "    <title>Please sign in</title>\n"
        + "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n"
        + "    <link href=\"https://getbootstrap.com/docs/4.0/examples/signin/signin.css\" rel=\"stylesheet\" crossorigin=\"anonymous\"/>\n"
        + "  </head>\n"
        + "  <body>\n"
        + "     <div class=\"container\">\n");

    String contextPath = request.getContextPath();
    sb.append("      <form class=\"form-signin\" method=\"post\" action=\"").append(contextPath)
        .append(this.authenticationUrl).append("\">\n")
        .append("        <h2 class=\"form-signin-heading\">Please sign in</h2>\n")
        .append(createError(loginError, errorMsg)).append(createLogoutSuccess(logoutSuccess))
        .append("        <p>\n")
        .append("          <label for=\"username\" class=\"sr-only\">Username</label>\n")
        .append("          <input type=\"text\" id=\"username\" name=\"")
        .append(this.usernameParameter)
        .append("\" class=\"form-control\" placeholder=\"Username\" required autofocus>\n")
        .append("        </p>\n").append("        <p>\n")
        .append("          <label for=\"password\" class=\"sr-only\">Password</label>\n")
        .append("          <input type=\"password\" id=\"password\" name=\"")
        .append(this.pwddParameter)
        .append("\" class=\"form-control\" placeholder=\"Password\" required>\n")
        .append("        </p>\n").append(renderHiddenInputs(request)).append(
        "        <button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Sign in</button>\n")
        .append("      </form>\n");

    sb.append("</div>\n");
    sb.append("</body></html>");

    return sb.toString();
  }

  private String renderHiddenInputs(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> input : this.resolveHiddenInputs.apply(request).entrySet()) {
      sb.append("<input name=\"").append(input.getKey()).append("\" type=\"hidden\" value=\"")
          .append(input.getValue()).append("\" />\n");
    }
    return sb.toString();
  }

  private boolean isLogoutSuccess(HttpServletRequest request) {
    return "GET".equals(request.getMethod()) && matches(request,
        logoutSuccessUrl);
  }

  private boolean isLoginUrlRequest(HttpServletRequest request) {
    return "GET".equals(request.getMethod()) && matches(request, loginPageUrl);
  }

  private boolean isErrorPage(HttpServletRequest request) {
    return "GET".equals(request.getMethod()) && matches(request, failureUrl);
  }

  private static String createError(boolean isError, String message) {
    return isError ? "<div class=\"alert alert-danger\" role=\"alert\">" + HtmlUtils
        .htmlEscape(message) + "</div>" : "";
  }

  private static String createLogoutSuccess(boolean isLogoutSuccess) {
    return isLogoutSuccess
        ? "<div class=\"alert alert-success\" role=\"alert\">You have been signed out</div>" : "";
  }

  private boolean matches(HttpServletRequest request, String url) {
    if (url == null) {
      return false;
    }
    String uri = request.getRequestURI();
    int pathParamIndex = uri.indexOf(';');

    if (pathParamIndex > 0) {
      // strip everything after the first semi-colon
      uri = uri.substring(0, pathParamIndex);
    }

    if (request.getQueryString() != null) {
      uri += "?" + request.getQueryString();
    }

    if ("".equals(request.getContextPath())) {
      return uri.equals(url);
    }

    return uri.equals(request.getContextPath() + url);
  }
}
