package top.bettercode.summer.tools.sap.stock;

import java.util.Objects;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class StockQuery {

  private String factoryId;
  private String materialId;

  public StockQuery(String factoryId, String materialId) {
    this.factoryId = factoryId;
    this.materialId = materialId;
  }

  public String getFactoryId() {
    return factoryId;
  }

  public void setFactoryId(String factoryId) {
    this.factoryId = factoryId;
  }

  public String getMaterialId() {
    return materialId;
  }

  public void setMaterialId(String materialId) {
    this.materialId = materialId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StockQuery)) {
      return false;
    }
    StockQuery that = (StockQuery) o;
    return Objects.equals(factoryId, that.factoryId) && Objects.equals(materialId,
        that.materialId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(factoryId, materialId);
  }

  @Override
  public String toString() {
    return StringUtil.json(this, true);
  }
}
