package top.bettercode.simpleframework.web.serializer;

/**
 * @author Peter Wu
 */
public class PathUrl {

  private String path;

  private String pathUrl;


  public PathUrl(String path, String pathUrl) {
    this.path = path;
    this.pathUrl = pathUrl;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPathUrl() {
    return pathUrl;
  }

  public void setPathUrl(String pathUrl) {
    this.pathUrl = pathUrl;
  }
}
