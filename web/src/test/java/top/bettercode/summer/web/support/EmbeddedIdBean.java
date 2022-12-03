package top.bettercode.summer.web.support;

import java.util.Objects;

public class EmbeddedIdBean {

  private String name;
  private Integer intCode;
  private Long price;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EmbeddedIdBean)) {
      return false;
    }
    EmbeddedIdBean that = (EmbeddedIdBean) o;
    return Objects.equals(name, that.name) && Objects.equals(intCode,
        that.intCode) && Objects.equals(price, that.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, intCode, price);
  }

  @Override
  public String toString() {
    return "EmbeddedIdBean{" +
        "name='" + name + '\'' +
        ", intCode=" + intCode +
        ", price=" + price +
        '}';
  }
}