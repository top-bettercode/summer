package top.bettercode.simpleframework.web.resolver.multipart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.lang.util.FileUtil;
import top.bettercode.simpleframework.web.serializer.annotation.JsonUrl;

/**
 * @author Peter Wu
 * @since 0.1.31
 */
public class Attachment implements Serializable {

  private static final long serialVersionUID = 1L;

  private final static Logger log = LoggerFactory.getLogger(Attachment.class);
  private String name;
  @JsonUrl
  private String path;
  @JsonIgnore
  private File file;

  public Attachment(String name, String path, File file) {
    this.name = name;
    this.path = path;
    this.file = file;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public long getLength() {
    return this.file.length();
  }

  //--------------------------------------------
  public boolean delete() {
    if (file != null) {
      boolean delete = FileUtil.delete(file);
      if (delete) {
        log.info("删除出错请求上传的文件：{}", file);
      }
      return delete;
    }
    return false;
  }
}
