package cn.bestwu.simpleframework.web.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  public static final int DEFAULT_ORDER = -9900;
  private int order = DEFAULT_ORDER;

  private String appVersionName;
  private String appVersion;

  private String appVersionNoName;
  private String appVersionNo;


  public ApiVersionFilter(String appVersionName, String appVersion,
      String appVersionNoName, String appVersionNo) {
    this.appVersionName = appVersionName;
    this.appVersion = appVersion;
    this.appVersionNoName = appVersionNoName;
    this.appVersionNo = appVersionNo;
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
  protected void doFilterInternal(final HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    response.setHeader(appVersionName, appVersion);
    response.setHeader(appVersionNoName, appVersionNo);
    filterChain.doFilter(request, response);
  }

}

