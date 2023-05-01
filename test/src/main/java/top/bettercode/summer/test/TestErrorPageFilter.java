package top.bettercode.summer.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.web.properties.SummerWebProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * A Servlet {@link Filter} that provides an {@link ErrorPageRegistry} for non-embedded applications
 * (i.e. deployed WAR files). It registers error pages and handles application errors by filtering
 * requests and forwarding to the error pages instead of letting the server handle them. Error pages
 * are a feature of the servlet spec but there is no Java API for registering them in the spec. This
 * filter works around that by accepting error page registrations from Spring Boot's
 * {@link ErrorPageRegistrar} (any beans of that type in the context will be applied to this
 * server).
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class TestErrorPageFilter implements Filter, ErrorPageRegistry, Ordered {

  private static final Log logger = LogFactory.getLog(TestErrorPageFilter.class);

  // From RequestDispatcher but not referenced to remain compatible with Servlet 2.5

  private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";

  private static final String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";

  private static final String ERROR_MESSAGE = "javax.servlet.error.message";

  /**
   * The name of the servlet attribute containing request URI.
   */
  public static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

  private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

  private static final Set<Class<?>> CLIENT_ABORT_EXCEPTIONS;

  static {
    Set<Class<?>> clientAbortExceptions = new HashSet<>();
    addClassIfPresent(clientAbortExceptions, "org.apache.catalina.connector.ClientAbortException");
    CLIENT_ABORT_EXCEPTIONS = Collections.unmodifiableSet(clientAbortExceptions);
  }

  private String global;

  private final Map<Integer, String> statuses = new HashMap<>();

  private final Map<Class<?>, String> exceptions = new HashMap<>();

  private final OncePerRequestFilter delegate = new OncePerRequestFilter() {

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
        @NotNull FilterChain chain) throws IOException {
      TestErrorPageFilter.this.doFilter(request, response, chain);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
      return false;
    }

  };

  private final BasicErrorController errorController;
  private final SummerWebProperties summerWebProperties;

  public TestErrorPageFilter(BasicErrorController errorController,
                             SummerWebProperties summerWebProperties) {
    this.errorController = errorController;
    this.summerWebProperties = summerWebProperties;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.delegate.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    this.delegate.doFilter(request, response, chain);
  }

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException {
    ErrorWrapperResponse wrapped = new ErrorWrapperResponse(response);
    try {
      chain.doFilter(request, wrapped);
      if (wrapped.hasErrorToSend()) {
        handleErrorStatus(request, response, wrapped.getStatus(), wrapped.getMessage());
        response.flushBuffer();
      } else if (!request.isAsyncStarted() && !response.isCommitted()) {
        response.flushBuffer();
      }
    } catch (Throwable ex) {
      Throwable exceptionToHandle = ex;
      if (ex instanceof ServletException) {
        Throwable rootCause = ((ServletException) ex).getRootCause();
        if (rootCause != null) {
          exceptionToHandle = rootCause;
        }
      }
      handleException(request, response, wrapped, exceptionToHandle);
      response.flushBuffer();
    }
  }

  private void handleErrorStatus(HttpServletRequest request, HttpServletResponse response,
      int status, String message) throws IOException {
    if (response.isCommitted()) {
      handleCommittedResponse(request, null);
      return;
    }
    response.setStatus(status);
    setErrorAttributes(request, status, message);
    handleError(request, response);
  }

  private void handleError(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ResponseEntity<Map<String, Object>> responseEntity = errorController.error(request);
    if (summerWebProperties.okEnable(request)) {
      response.setStatus(HttpStatus.OK.value());
    }
    StringUtil.getOBJECT_MAPPER().writeValue(response.getOutputStream(), responseEntity.getBody());
    response.flushBuffer();
  }

  private void handleException(HttpServletRequest request, HttpServletResponse response,
      ErrorWrapperResponse wrapped,
      Throwable ex) throws IOException {
    if (response.isCommitted()) {
      handleCommittedResponse(request, ex);
      return;
    }
    forwardToErrorPage(request, wrapped, ex);
  }

  private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
      Throwable ex) throws IOException {
    setErrorAttributes(request, 500, ex.getMessage());
    request.setAttribute(ERROR_EXCEPTION, ex);
    request.setAttribute(ERROR_EXCEPTION_TYPE, ex.getClass());
    response.reset();
    response.setStatus(500);
    handleError(request, response);
    request.removeAttribute(ERROR_EXCEPTION);
    request.removeAttribute(ERROR_EXCEPTION_TYPE);
  }

  /**
   * Return the description for the given request. By default this method will return a description
   * based on the request {@code servletPath} and {@code pathInfo}.
   *
   * @param request the source request
   * @return the description
   * @since 1.5.0
   */
  protected String getDescription(HttpServletRequest request) {
    String pathInfo = (request.getPathInfo() != null) ? request.getPathInfo() : "";
    return "[" + request.getServletPath() + pathInfo + "]";
  }

  private void handleCommittedResponse(HttpServletRequest request, Throwable ex) {
    if (isClientAbortException(ex)) {
      return;
    }
    String message = "Cannot forward to error page for request " + getDescription(request)
        + " as the response has already been"
        + " committed. As a result, the response may have the wrong status"
        + " code. If your application is running on WebSphere Application"
        + " Server you may be able to resolve this problem by setting"
        + " com.ibm.ws.webcontainer.invokeFlushAfterService to false";
    if (ex == null) {
      logger.error(message);
    } else {
      // User might see the error page without all the data here but throwing the
      // exception isn't going to help anyone (we'll log it to be on the safe side)
      logger.error(message, ex);
    }
  }

  private boolean isClientAbortException(Throwable ex) {
    if (ex == null) {
      return false;
    }
    for (Class<?> candidate : CLIENT_ABORT_EXCEPTIONS) {
      if (candidate.isInstance(ex)) {
        return true;
      }
    }
    return isClientAbortException(ex.getCause());
  }

  private String getErrorPath(Map<Integer, String> map, Integer status) {
    if (map.containsKey(status)) {
      return map.get(status);
    }
    return this.global;
  }

  private String getErrorPath(Class<?> type) {
    while (type != Object.class) {
      String path = this.exceptions.get(type);
      if (path != null) {
        return path;
      }
      type = type.getSuperclass();
    }
    return this.global;
  }

  private void setErrorAttributes(HttpServletRequest request, int status, String message) {
    request.setAttribute(ERROR_STATUS_CODE, status);
    request.setAttribute(ERROR_MESSAGE, message);
    request.setAttribute(ERROR_REQUEST_URI, request.getRequestURI());
  }

  private void rethrow(Throwable ex) throws IOException, ServletException {
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    if (ex instanceof IOException) {
      throw (IOException) ex;
    }
    if (ex instanceof ServletException) {
      throw (ServletException) ex;
    }
    throw new IllegalStateException(ex);
  }

  @Override
  public void addErrorPages(ErrorPage... errorPages) {
    for (ErrorPage errorPage : errorPages) {
      if (errorPage.isGlobal()) {
        this.global = errorPage.getPath();
      } else if (errorPage.getStatus() != null) {
        this.statuses.put(errorPage.getStatus().value(), errorPage.getPath());
      } else {
        this.exceptions.put(errorPage.getException(), errorPage.getPath());
      }
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }

  private static void addClassIfPresent(Collection<Class<?>> collection, String className) {
    try {
      collection.add(ClassUtils.forName(className, null));
    } catch (Throwable ignored) {
    }
  }

  private static class ErrorWrapperResponse extends HttpServletResponseWrapper {

    private int status;

    private String message;

    private boolean hasErrorToSend = false;

    ErrorWrapperResponse(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void sendError(int status) {
      sendError(status, null);
    }

    @Override
    public void sendError(int status, String message) {
      this.status = status;
      this.message = message;
      this.hasErrorToSend = true;
      // Do not call super because the container may prevent us from handling the
      // error ourselves
    }

    @Override
    public int getStatus() {
      if (this.hasErrorToSend) {
        return this.status;
      }
      // If there was no error we need to trust the wrapped response
      return super.getStatus();
    }

    @Override
    public void flushBuffer() throws IOException {
      sendErrorIfNecessary();
      super.flushBuffer();
    }

    private void sendErrorIfNecessary() {
      if (this.hasErrorToSend && !isCommitted()) {
//        ((HttpServletResponse) getResponse()).sendError(this.status, this.message);
      }
    }

    String getMessage() {
      return this.message;
    }

    boolean hasErrorToSend() {
      return this.hasErrorToSend;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      sendErrorIfNecessary();
      return super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      sendErrorIfNecessary();
      return super.getOutputStream();
    }

  }

}
