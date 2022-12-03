package top.bettercode.summer.web.filter;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * 对delete,put隐藏方法的处理
 *
 * @author Peter Wu
 */
public class OrderedHiddenHttpMethodFilter extends HiddenHttpMethodFilter implements Ordered {

  /**
   * The default order is high to ensure the filter is applied before Spring Security.
   */
  public static final int DEFAULT_ORDER = -10000;
  /**
   * Default method parameter: {@code _method}
   */
  public static final String DEFAULT_METHOD_PARAM = "_method";
  private int order = DEFAULT_ORDER;
  private String methodParam = DEFAULT_METHOD_PARAM;

  /**
   * Set the parameter name to look for HTTP methods.
   *
   * @param methodParam methodParam
   * @see #DEFAULT_METHOD_PARAM
   */
  public void setMethodParam(@NotNull String methodParam) {
    Assert.hasText(methodParam, "'methodParam' must not be empty");
    this.methodParam = methodParam;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  /**
   * Set the order for this filter.
   *
   * @param order the order to set
   */
  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
      throws ServletException, IOException {

    String paramValue = request.getParameter(this.methodParam);
    if (StringUtils.hasLength(paramValue)) {
      String _method = request.getMethod();
      String method = paramValue.toUpperCase(Locale.ENGLISH);
      if (("POST".equals(_method) && "PUT".equals(method))
          || ("GET".equals(_method) && "DELETE".equals(method))) {
        HttpServletRequest wrapper = new HttpMethodRequestWrapper(
            request, method);
        filterChain.doFilter(wrapper, response);
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  /**
   * Simple {@link HttpServletRequest} wrapper that returns the supplied method for {@link
   * HttpServletRequest#getMethod()}.
   */
  private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

    private final String method;

    public HttpMethodRequestWrapper(HttpServletRequest request, String method) {
      super(request);
      this.method = method;
    }

    @Override
    public String getMethod() {
      return this.method;
    }
  }
}

