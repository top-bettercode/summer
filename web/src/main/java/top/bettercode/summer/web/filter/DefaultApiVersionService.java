package top.bettercode.summer.web.filter;

import top.bettercode.summer.web.config.SummerWebProperties;

/**
 * @author Peter Wu
 */
public class DefaultApiVersionService implements ApiVersionService{

  private final SummerWebProperties summerWebProperties;

  public DefaultApiVersionService(SummerWebProperties summerWebProperties) {
    this.summerWebProperties = summerWebProperties;
  }

  @Override
  public String getVersionName() {
    return summerWebProperties.getVersionName();
  }

  @Override
  public String getVersion() {
    return summerWebProperties.getVersion();
  }

  @Override
  public String getVersionNoName() {
    return summerWebProperties.getVersionNoName();
  }

  @Override
  public String getVersionNo() {
    return summerWebProperties.getVersionNo();
  }

}
