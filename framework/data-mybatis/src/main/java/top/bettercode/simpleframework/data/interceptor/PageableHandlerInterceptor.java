package top.bettercode.simpleframework.data.interceptor;

import top.bettercode.simpleframework.data.Pageable;
import com.baomidou.mybatisplus.plugins.pagination.PageHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * Mybatis Page 参数解析
 *
 * @author Peter Wu
 */
public class PageableHandlerInterceptor implements AsyncHandlerInterceptor {

  private static final String DEFAULT_PAGE_PARAMETER = "page";
  private static final String DEFAULT_SIZE_PARAMETER = "size";
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_MAX_PAGE_SIZE = 2000;

  private String pageParameterName = DEFAULT_PAGE_PARAMETER;
  private String sizeParameterName = DEFAULT_SIZE_PARAMETER;
  private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;
  private boolean oneIndexedParameters = false;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    if (supportsHandler(handler)) {
      String pageString = request.getParameter(pageParameterName);
      String pageSizeString = request.getParameter(sizeParameterName);

      int page = parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, true);
      int pageSize =
          StringUtils.hasText(pageSizeString) ? parseAndApplyBoundaries(pageSizeString, maxPageSize,
              false) : DEFAULT_PAGE_SIZE;

      PageHelper.startPage(page, pageSize);
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    if (supportsHandler(handler)) {
      PageHelper.remove();
    }
  }

  private boolean supportsHandler(Object handler) {
    return handler instanceof HandlerMethod && ((HandlerMethod) handler)
        .hasMethodAnnotation(Pageable.class);
  }

  private int parseAndApplyBoundaries(String parameter, int upper, boolean shiftIndex) {

    try {
      int parsed = Integer.parseInt(parameter) - (oneIndexedParameters && shiftIndex ? 1 : 0);
      return parsed < 1 ? 1 : Math.min(parsed, upper);
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  public void setPageParameterName(String pageParameterName) {
    this.pageParameterName = pageParameterName;
  }

  public void setSizeParameterName(String sizeParameterName) {
    this.sizeParameterName = sizeParameterName;
  }

  public void setMaxPageSize(int maxPageSize) {
    this.maxPageSize = maxPageSize;
  }

  public void setOneIndexedParameters(boolean oneIndexedParameters) {
    this.oneIndexedParameters = oneIndexedParameters;
  }
}
