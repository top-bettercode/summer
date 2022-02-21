package top.bettercode.simpleframework.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EmbeddedIdBean {

  private BigDecimal number1;
  private String name;
  private Integer intCode;
  private Long price;
  private LocalDateTime date;
  private List<String> paths;
  private String[] pathArray;

  public BigDecimal getNumber1() {
    return number1;
  }

  public void setNumber1(BigDecimal number1) {
    this.number1 = number1;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getIntCode() {
    return intCode;
  }

  public void setIntCode(Integer intCode) {
    this.intCode = intCode;
  }

  public Long getPrice() {
    return price;
  }

  public void setPrice(Long price) {
    this.price = price;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EmbeddedIdBean)) {
      return false;
    }
    EmbeddedIdBean that = (EmbeddedIdBean) o;
    return Objects.equals(number1, that.number1) && Objects.equals(name,
        that.name) && Objects.equals(intCode, that.intCode) && Objects.equals(
        price, that.price) && Objects.equals(date, that.date) && Objects.equals(
        paths, that.paths) && Arrays.equals(pathArray, that.pathArray);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(number1, name, intCode, price, date, paths);
    result = 31 * result + Arrays.hashCode(pathArray);
    return result;
  }

  @Override
  public String toString() {
    return "EmbeddedIdBean{" +
        "number1=" + number1 +
        ", name='" + name + '\'' +
        ", intCode=" + intCode +
        ", price=" + price +
        ", date=" + date +
        ", paths=" + paths +
        ", pathArray=" + Arrays.toString(pathArray) +
        '}';
  }
}