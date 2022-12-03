package top.bettercode.summer.data.jpa.config;

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
    private Object trueValue = 1;
    /**
     * 默认逻辑未删除值.
     */
    private Object falseValue = 0;

    public Object getTrueValue() {
      return trueValue;
    }

    public void setTrueValue(Object trueValue) {
      this.trueValue = trueValue;
    }

    public Object getFalseValue() {
      return falseValue;
    }

    public void setFalseValue(Object falseValue) {
      this.falseValue = falseValue;
    }
  }
}
