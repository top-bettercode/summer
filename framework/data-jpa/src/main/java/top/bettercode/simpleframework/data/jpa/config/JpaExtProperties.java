package top.bettercode.simpleframework.data.jpa.config;

/**
 * @author Peter Wu
 */
public class JpaExtProperties {

  private SoftDelete softDelete = new SoftDelete();

  public SoftDelete getSoftDelete() {
    return softDelete;
  }

  public void setSoftDelete(SoftDelete softDelete) {
    this.softDelete = softDelete;
  }

  public static class SoftDelete {

    /**
     * 默认逻辑删除值.
     */
    private String trueValue = "true";
    /**
     * 默认逻辑未删除值.
     */
    private String falseValue = "false";

    public String getTrueValue() {
      return trueValue;
    }

    public void setTrueValue(String trueValue) {
      this.trueValue = trueValue;
    }

    public String getFalseValue() {
      return falseValue;
    }

    public void setFalseValue(String falseValue) {
      this.falseValue = falseValue;
    }
  }
}
