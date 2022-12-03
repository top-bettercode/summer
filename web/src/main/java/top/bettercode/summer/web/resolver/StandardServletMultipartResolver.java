package top.bettercode.summer.web.resolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

/**
 * 支持PUT上传
 *
 * @author Peter Wu
 */
public class StandardServletMultipartResolver implements MultipartResolver {

  private final Logger log = LoggerFactory.getLogger(StandardServletMultipartResolver.class);
  private boolean resolveLazily = false;

  public void setResolveLazily(boolean resolveLazily) {
    this.resolveLazily = resolveLazily;
  }

  @Override
  public boolean isMultipart(HttpServletRequest request) {
    // Same check as in Commons FileUpload...
    String method = request.getMethod();
    if (RequestMethod.POST.name().equals(method) || RequestMethod.PUT.name()
        .equals(method)) {//支持PUT方法
      String contentType = request.getContentType();
      return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
    }
    return false;
  }

  @NotNull
  @Override
  public MultipartHttpServletRequest resolveMultipart(@NotNull HttpServletRequest request)
      throws MultipartException {
    return new StandardMultipartHttpServletRequest(request, this.resolveLazily);
  }

  @Override
  public void cleanupMultipart(@NotNull MultipartHttpServletRequest request) {
    // To be on the safe side: explicitly delete the parts,
    // but only actual file parts (for Resin compatibility)
    try {
      for (Part part : request.getParts()) {
        if (request.getFile(part.getName()) != null) {
          part.delete();
        }
      }
    } catch (Exception ex) {
      if (log.isWarnEnabled()) {
        log.warn("Failed to perform cleanup of multipart items", ex);
      }
    }
  }

}
