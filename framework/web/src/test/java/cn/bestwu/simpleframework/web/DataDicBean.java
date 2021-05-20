package cn.bestwu.simpleframework.web;

import cn.bestwu.simpleframework.web.resolver.Cent;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonBigDecimal;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonUrl;
import java.math.BigDecimal;
import java.util.List;

public class DataDicBean {

  private BigDecimal number1;
  @JsonBigDecimal(scale = 3)
  private BigDecimal number2;
  @JsonBigDecimal(scale = 4, toPlainString = true)
  private BigDecimal number3;
  @JsonBigDecimal(scale = 4, toPlainString = true, reduceFraction = true)
  private BigDecimal number4;


  private String name;
  private String code;
  private Integer intCode;
  @Cent
  private Long price;
  @JsonUrl
  private String path;
  private String path1;
  private String desc;

  private List<String> paths;
  private String[] pathArray;


  public BigDecimal getNumber1() {
    return number1;
  }

  public void setNumber1(BigDecimal number1) {
    this.number1 = number1;
  }

  public BigDecimal getNumber2() {
    return number2;
  }

  public void setNumber2(BigDecimal number2) {
    this.number2 = number2;
  }

  public BigDecimal getNumber3() {
    return number3;
  }

  public void setNumber3(BigDecimal number3) {
    this.number3 = number3;
  }


  public BigDecimal getNumber4() {
    return number4;
  }

  public void setNumber4(BigDecimal number4) {
    this.number4 = number4;
  }

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