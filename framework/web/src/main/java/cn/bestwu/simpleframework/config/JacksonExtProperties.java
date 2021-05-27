package cn.bestwu.simpleframework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.spring.jackson")
public class JacksonExtProperties {

  /**
   * xml root name.
   */
  private String xmlRootName = "xml";
  /**
   * Feature that controls whether XML declaration should be written before when generator is
   * initialized (true) or not (false).
   */
  private Boolean writeXmlDeclaration = false;

  /**
   * Feature that controls whether null should be serialize as empty.
   */
  private Boolean defaultEmpty = false;

  /**
   * specifying the packages to scan for mixIn annotation.
   */
  private String[] mixInAnnotationBasePackages = new String[0];

  public String getXmlRootName() {
    return xmlRootName;
  }

  public void setXmlRootName(String xmlRootName) {
    this.xmlRootName = xmlRootName;
  }

  public Boolean getWriteXmlDeclaration() {
    return writeXmlDeclaration;
  }

  public void setWriteXmlDeclaration(Boolean writeXmlDeclaration) {
    this.writeXmlDeclaration = writeXmlDeclaration;
  }

  public Boolean getDefaultEmpty() {
    return defaultEmpty;
  }

  public void setDefaultEmpty(Boolean defaultEmpty) {
    this.defaultEmpty = defaultEmpty;
  }

  public String[] getMixInAnnotationBasePackages() {
    return mixInAnnotationBasePackages;
  }

  public void setMixInAnnotationBasePackages(String[] mixInAnnotationBasePackages) {
    this.mixInAnnotationBasePackages = mixInAnnotationBasePackages;
  }
}
