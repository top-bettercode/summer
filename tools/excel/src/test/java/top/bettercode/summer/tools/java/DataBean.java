package top.bettercode.summer.tools.java;

import java.math.BigDecimal;
import java.util.Date;
import top.bettercode.summer.tools.lang.util.RandomUtil;

public class DataBean {

  private Integer intCode;
  private Integer integer;
  private Long longl;
  private Double doublel;
  private Float floatl;
  private String name;
  private BigDecimal num;
  private Date date;

  public DataBean() {
    intCode = 1;
    integer = 1;
    longl = new Date().getTime();
    doublel = 4.4;
    floatl = 5.5f;
    num = new BigDecimal("0." + RandomUtil.nextInt(2));
    name = "名称";
    date = new Date();
  }

  public DataBean(Integer index) {
    intCode = 1 + index / 3;
    integer = 1 + index / 2;
    longl = new Date().getTime() + index * 10000;
    doublel = 4.4 + index;
    floatl = 5.5f + index;
    num = new BigDecimal("0." + index);
    name = "名称" + index;
    date = new Date();
  }

  public BigDecimal getNum() {
    return num;
  }

  public void setNum(BigDecimal num) {
    this.num = num;
  }

  public Integer getIntCode() {
    return intCode;
  }

  public void setIntCode(Integer intCode) {
    this.intCode = intCode;
  }

  public Integer getInteger() {
    return integer;
  }

  public void setInteger(Integer integer) {
    this.integer = integer;
  }

  public Long getLongl() {
    return longl;
  }

  public void setLongl(Long longl) {
    this.longl = longl;
  }

  public Double getDoublel() {
    return doublel;
  }

  public DataBean setDoublel(Double doublel) {
    this.doublel = doublel;
    return this;
  }

  public Float getFloatl() {
    return floatl;
  }

  public DataBean setFloatl(Float floatl) {
    this.floatl = floatl;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
