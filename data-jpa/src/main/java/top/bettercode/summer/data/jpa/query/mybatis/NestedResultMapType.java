package top.bettercode.summer.data.jpa.query.mybatis;

/**
 * @author Peter Wu
 */
public class NestedResultMapType {

  private final String nestedResultMapId;
  private final boolean isCollection;

  public NestedResultMapType(String nestedResultMapId, boolean isCollection) {
    this.nestedResultMapId = nestedResultMapId;
    this.isCollection = isCollection;
  }

  public String getNestedResultMapId() {
    return nestedResultMapId;
  }

  public boolean isCollection() {
    return isCollection;
  }
}

