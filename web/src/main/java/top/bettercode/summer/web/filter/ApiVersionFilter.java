package top.bettercode.summer.web.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 支持PUT DELETE form提交
 *
 * @author Peter Wu
 */
public class ApiVersionFilter extends OncePerRequestFilter implements Ordered {

  /**
   * Higher order to ensure the filter is applied before Spring Security.
   */
  public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1;
  private int order = DEFAULT_ORDER;

  private final ApiVersionService apiVersionService;

  public ApiVersionFilter(ApiVersionService apiVersionService) {
    this.apiVersionService = apiVersionService;
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
  protected void doFilterInternal(@NotNull final HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    response.setHeader(apiVersionService.getVersionName(), apiVersionService.getVersion());
    response.setHeader(apiVersionService.getVersionNoName(), apiVersionService.getVersionNo());
    filterChain.doFilter(request, response);
  }

}

