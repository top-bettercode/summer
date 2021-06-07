package top.bettercode.simpleframework.data.resolver;

import com.baomidou.mybatisplus.plugins.Page;
import java.awt.print.Pageable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Page 参数解析
 *
 * @author Peter Wu
 */
public class PagHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String DEFAULT_PAGE_PARAMETER = "page";
  private static final String DEFAULT_SIZE_PARAMETER = "size";
  private static final String DEFAULT_PREFIX = "";
  private static final String DEFAULT_QUALIFIER_DELIMITER = "_";
  private static final int DEFAULT_MAX_PAGE_SIZE = 2000;
  private static final Page<?> DEFAULT_PAGE_REQUEST = new Page<>(1, 20);

  private Page<?> fallbackPageable = DEFAULT_PAGE_REQUEST;
  private String pageParameterName = DEFAULT_PAGE_PARAMETER;
  private String sizeParameterName = DEFAULT_SIZE_PARAMETER;
  private String prefix = DEFAULT_PREFIX;
  private String qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER;
  private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;
  private boolean oneIndexedParameters = false;

  public void setFallbackPageable(Page<?> fallbackPageable) {
    this.fallbackPageable = fallbackPageable;
  }

  public boolean isFallbackPageable(Pageable pageable) {
    return this.fallbackPageable.equals(pageable);
  }

  protected int getMaxPageSize() {
    return this.maxPageSize;
  }

  public void setMaxPageSize(int maxPageSize) {
    this.maxPageSize = maxPageSize;
  }

  protected String getPageParameterName() {
    return this.pageParameterName;
  }

  public void setPageParameterName(String pageParameterName) {

    Assert.hasText(pageParameterName, "Page parameter name must not be null or empty!");
    this.pageParameterName = pageParameterName;
  }

  protected String getSizeParameterName() {
    return this.sizeParameterName;
  }

  public void setSizeParameterName(String sizeParameterName) {

    Assert.hasText(sizeParameterName, "Size parameter name must not be null or empty!");
    this.sizeParameterName = sizeParameterName;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
  }

  public void setQualifierDelimiter(String qualifierDelimiter) {
    this.qualifierDelimiter =
        qualifierDelimiter == null ? DEFAULT_QUALIFIER_DELIMITER : qualifierDelimiter;
  }

  protected boolean isOneIndexedParameters() {
    return this.oneIndexedParameters;
  }

  public void setOneIndexedParameters(boolean oneIndexedParameters) {
    this.oneIndexedParameters = oneIndexedParameters;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().isAssignableFrom(Page.class);
  }

  @Override
  public Page<?> resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

    Page<?> defaultOrFallback = getDefaultFromAnnotationOrFallback(methodParameter);

    String pageString = webRequest
        .getParameter(getParameterNameToUse(pageParameterName, methodParameter));
    String pageSizeString = webRequest
        .getParameter(getParameterNameToUse(sizeParameterName, methodParameter));

    int page = parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, true);
    int pageSize =
        StringUtils.hasText(pageSizeString) ? parseAndApplyBoundaries(pageSizeString, maxPageSize,
            false)
            : defaultOrFallback.getSize();

    return new Page<>(page, pageSize);
  }

  protected String getParameterNameToUse(String source, MethodParameter parameter) {

    StringBuilder builder = new StringBuilder(prefix);

    if (parameter != null && parameter.hasParameterAnnotation(Qualifier.class)) {
      builder.append(parameter.getParameterAnnotation(Qualifier.class).value());
      builder.append(qualifierDelimiter);
    }

    return builder.append(source).toString();
  }

  private Page<?> getDefaultFromAnnotationOrFallback(MethodParameter methodParameter) {
    return fallbackPageable;
  }

  private int parseAndApplyBoundaries(String parameter, int upper, boolean shiftIndex) {

    try {
      int parsed = Integer.parseInt(parameter) - (oneIndexedParameters && shiftIndex ? 1 : 0);
      return parsed < 1 ? 1 : Math.min(parsed, upper);
    } catch (NumberFormatException e) {
      return 1;
    }
  }
}