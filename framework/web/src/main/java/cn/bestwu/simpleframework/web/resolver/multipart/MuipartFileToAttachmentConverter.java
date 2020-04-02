package cn.bestwu.simpleframework.web.resolver.multipart;

import cn.bestwu.lang.util.FilenameUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

/**
 * MultipartFile 转换为对应的url或路径。
 *
 * @author Peter Wu
 */
public class MuipartFileToAttachmentConverter implements Converter<MultipartFile, Attachment> {

  public static final String FILE_TYPE_PARAM_TYPE = "fileType";
  public static final String REQUEST_FILES = "REQUEST_FILES";
  private Logger log = LoggerFactory.getLogger(MuipartFileToAttachmentConverter.class);

  private final MultipartProperties multipartProperties;

  public MuipartFileToAttachmentConverter(MultipartProperties multipartProperties) {
    this.multipartProperties = multipartProperties;
  }

  @Override
  public Attachment convert(MultipartFile source) {
    try {
      ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
          .getRequestAttributes();
      HttpServletRequest request = requestAttributes.getRequest();

      String name = source.getName();
      if (source.isEmpty()) {
        throw new IllegalArgumentException("不能上传空文件");
      }

      String fileType = (String) request.getAttribute(FILE_TYPE_PARAM_TYPE);
      if (fileType == null) {
        @SuppressWarnings("unchecked") Collection<String> values = ((Map<String, String>) request
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).values();
        if (values.isEmpty()) {
          fileType = request.getParameter(FILE_TYPE_PARAM_TYPE);
        } else {
          fileType = values.iterator().next();
        }
        if (!StringUtils.hasText(fileType)) {
          fileType = multipartProperties.getDefaultFileType();
        }
      }
      String filePath = File.separator + fileType + File.separator;
      filePath += LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
      String originalFilename = source.getOriginalFilename();
      boolean hasExtension = StringUtils.hasText(originalFilename);
      String extension = hasExtension ? FilenameUtil.getExtension(originalFilename) : "";
      String path;
      if (multipartProperties.isKeepOriginalFilename()) {
        filePath += File.separator + UUID.randomUUID().toString() + File.separator;
        String nameWithoutExtension = StringUtils
            .trimAllWhitespace(FilenameUtil.getNameWithoutExtension(originalFilename));
        path = filePath + URLEncoder.encode(nameWithoutExtension, "UTF-8");
        filePath += nameWithoutExtension;
      } else {
        filePath += File.separator + UUID.randomUUID().toString();
        path = filePath;
      }
      if (hasExtension) {
        filePath += "." + extension;
        path += "." + extension;
      }

      String baseSavePath = multipartProperties.getBaseSavePath();
      File dest = new File(baseSavePath, filePath);
      File parentFile = dest.getParentFile();
      if (!parentFile.exists()) {
        parentFile.mkdirs();
      }
      StreamUtils.copy(source.getInputStream(), new FileOutputStream(dest));

      if (log.isDebugEnabled()) {
        log.debug("上传文件保存至：" + dest.getAbsolutePath());
      }
      Attachment attachment = new Attachment(originalFilename, path, dest);
      @SuppressWarnings("unchecked")
      MultiValueMap<String, Attachment> files = (MultiValueMap<String, Attachment>) request
          .getAttribute(REQUEST_FILES);
      if (files == null) {
        files = new LinkedMultiValueMap<>();
        request.setAttribute(REQUEST_FILES, files);
      }
      files.add(name, attachment);
      return attachment;
    } catch (Exception e) {
      cleanFile();
      log.error("转存文件失败", e);
      throw new IllegalArgumentException("转存文件失败", e);
    }
  }

  /**
   * 清除上传的文件
   */
  public void cleanFile() {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    HttpServletRequest request = requestAttributes.getRequest();
    @SuppressWarnings("unchecked") MultiValueMap<String, Attachment> files = (MultiValueMap<String, Attachment>) request
        .getAttribute(REQUEST_FILES);
    if (files != null) {
      for (List<Attachment> attachments : files.values()) {
        for (Attachment attachment : attachments) {
          attachment.delete();
        }
      }
    }
  }
}