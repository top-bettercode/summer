package top.bettercode.summer.tools.qvod.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import kotlin.collections.CollectionsKt;
import top.bettercode.summer.tools.qvod.QvodAntiLeechUrl;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataBean {
  @QvodAntiLeechUrl
  private String path="https://vod2.myqcloud.com/ad/dd/a.mp4";
  @QvodAntiLeechUrl(separator = ",")
  private String path1="https://vod2.myqcloud.com/ad/dd/a.mp4,https://vod2.myqcloud.com/ad/dd/b.mp4";

  @QvodAntiLeechUrl
  private List<String> paths= CollectionsKt.listOf("https://vod2.myqcloud.com/ad/dd/a.mp4","https://vod2.myqcloud.com/ad/dd/b.mp4");
  @QvodAntiLeechUrl
  private String[] pathArray=new String[]{
      "https://vod2.myqcloud.com/ad/dd/a.mp4","https://vod2.myqcloud.com/ad/dd/b.mp4"
  };

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath1() {
    return path1;
  }

  public void setPath1(String path1) {
    this.path1 = path1;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPaths(List<String> paths) {
    this.paths = paths;
  }

  public String[] getPathArray() {
    return pathArray;
  }

  public void setPathArray(String[] pathArray) {
    this.pathArray = pathArray;
  }
}