package top.bettercode.summer.web.xss;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * XSS过滤
 */
public class XssFilter extends OncePerRequestFilter implements Ordered {


  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(request);
    filterChain.doFilter(xssRequest, response);
  }
}