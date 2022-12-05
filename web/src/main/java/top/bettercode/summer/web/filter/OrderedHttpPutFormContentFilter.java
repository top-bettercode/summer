package top.bettercode.summer.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 支持PUT DELETE form提交
 *
 * @author Peter Wu
 */
public class OrderedHttpPutFormContentFilter extends OncePerRequestFilter implements Ordered {

  /**
   * Higher order to ensure the filter is applied before Spring Security.
   */
  public static final int DEFAULT_ORDER = -9900;
  private final FormHttpMessageConverter formConverter = new AllEncompassingFormHttpMessageConverter();
  private int order = DEFAULT_ORDER;

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

  /**
   * The default character set to use for reading form data.
   *
   * @param charset charset
   */
  public void setCharset(Charset charset) {
    this.formConverter.setCharset(charset);
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws ServletException, IOException {

    if (("PUT".equals(request.getMethod()) || "DELETE".equals(request.getMethod()) || "PATCH"
        .equals(request.getMethod())) && isFormContentType(request)) {
      HttpInputMessage inputMessage = new ServletServerHttpRequest(request) {
        @NotNull
        @Override
        public InputStream getBody() throws IOException {
          return request.getInputStream();
        }
      };
      MultiValueMap<String, String> formParameters = formConverter.read(null, inputMessage);
      HttpServletRequest wrapper = new HttpPutFormContentRequestWrapper(request, formParameters);
      filterChain.doFilter(wrapper, response);
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private boolean isFormContentType(HttpServletRequest request) {
    String contentType = request.getContentType();
    if (contentType != null) {
      try {
        MediaType mediaType = MediaType.parseMediaType(contentType);
        return (MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType));
      } catch (IllegalArgumentException ex) {
        return false;
      }
    } else {
      return false;
    }
  }

  private static class HttpPutFormContentRequestWrapper extends HttpServletRequestWrapper {

    private final MultiValueMap<String, String> formParameters;

    public HttpPutFormContentRequestWrapper(HttpServletRequest request,
        MultiValueMap<String, String> parameters) {
      super(request);
      this.formParameters =
          (parameters != null) ? parameters : new LinkedMultiValueMap<>();
    }

    @Override
    public String getParameter(String name) {
      String queryStringValue = super.getParameter(name);
      String formValue = this.formParameters.getFirst(name);
      return (queryStringValue != null) ? queryStringValue : formValue;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
      Map<String, String[]> result = new LinkedHashMap<>();
      Enumeration<String> names = this.getParameterNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();
        result.put(name, this.getParameterValues(name));
      }
      return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
      Set<String> names = new LinkedHashSet<>();
      names.addAll(Collections.list(super.getParameterNames()));
      names.addAll(this.formParameters.keySet());
      return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {
      String[] queryStringValues = super.getParameterValues(name);
      List<String> formValues = this.formParameters.get(name);
      if (formValues == null) {
        return queryStringValues;
      } else if (queryStringValues == null) {
        return formValues.toArray(new String[0]);
      } else {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(queryStringValues));
        result.addAll(formValues);
        return result.toArray(new String[0]);
      }
    }
  }

}

