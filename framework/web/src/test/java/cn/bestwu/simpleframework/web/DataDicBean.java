package cn.bestwu.simpleframework.web;

import cn.bestwu.simpleframework.web.resolver.Cent;
import java.util.List;

public class DataDicBean {

  private String name;
  private String code;
  private Integer intCode;
  @Cent
  private Long price;
  private String path;
  private String path1;
  private String desc;

  private List<String> paths;
  private String[] pathArray;

  public String getPath1() {
    return path1;
  }

  public void setPath1(String path1) {
    this.path1 = path1;
  }

  public String[] getPathArray() {
    return pathArray;
  }

  public void setPathArray(String[] pathArray) {
    this.pathArray = pathArray;
  }

  public List<String> getPaths1() {
    return paths;
  }


  public String[] getPathArray1() {
    return pathArray;
  }

  public Long getPrice() {
    return price;
  }

  public void setPrice(Long price) {
    this.price = price;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPaths(List<String> paths) {
    this.paths = paths;
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

  public Integer getIntCode() {
    return intCode;
  }

  public void setIntCode(Integer intCode) {
    this.intCode = intCode;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }
}