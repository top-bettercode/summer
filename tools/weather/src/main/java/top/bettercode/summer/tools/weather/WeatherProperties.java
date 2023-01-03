package top.bettercode.summer.tools.weather;

import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.weather")
public class WeatherProperties {

  /**
   * 接口地址
   * <p>
   * app 	string 	是 	weather.realtime weaId 	number 	否 	(推荐) 通过weaId查询，例: 1 城市列表城市列表中weaId
   * (参数weaId,cityId,cityNm,cityIp,wgs84ll中取1个作为查询条件) cityId 	string 	否 	(推荐) 城市编号查询，例: 101010100
   * 城市列表城市列表中cityId cityNm 	string 	否 	通过中文城市名查询，例: 北京 城市列表城市列表中cityNm (带入前urlencode) cityIp string
   * 	否 	通过ip地址查询，例: 202.104.153.201 wgs84ll 	string 	否 	通过经纬度坐标查询，例:116.442708,39.917344 （付费用户可用）
   * ag 	string 	否 	功能显示参数: today,futureDay,lifeIndex,futureHour today 显示今日天气节点 futureDay
   * 显示天气预报(未来5-7天)节点 lifeIndex 显示相关节点中生活指数节点 futureHour 显示天气预报(逐小时)节点 多个用逗号隔开,可灵活选择 appkey 	string
   * 是 	使用API的唯一凭证 获取 sign 	string 	是 	md5后的32位密文,登陆用. 获取 format 	{json|xml} 	否 	返回数据格式 jsoncallback
   * string 	否 	js跨域使用jsonp时可使用此参数
   * </p>
   */
  private String url = "https://sapi.k780.com";
  /**
   * 使用API的唯一凭证
   */
  private String appKey;

  /**
   * md5后的32位密文,登陆用.
   */
  private String sign;

  /**
   * 晚上起始时间
   */
  private LocalTime nightStartTime = LocalTime.of(18, 0);
  /**
   * 晚上结束时间
   */
  private LocalTime nightEndTime = LocalTime.of(6, 0);

  /**
   * 请求连接超时时间毫秒数
   */
  private int connectTimeout = 10000;
  /**
   * 请求读取超时时间毫秒数
   */
  private int readTimeout = 10000;

  public String getUrl() {
    return url;
  }

  public WeatherProperties setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getAppKey() {
    return appKey;
  }

  public WeatherProperties setAppKey(String appKey) {
    this.appKey = appKey;
    return this;
  }

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public LocalTime getNightStartTime() {
    return nightStartTime;
  }

  public void setNightStartTime(LocalTime nightStartTime) {
    this.nightStartTime = nightStartTime;
  }

  public LocalTime getNightEndTime() {
    return nightEndTime;
  }

  public void setNightEndTime(LocalTime nightEndTime) {
    this.nightEndTime = nightEndTime;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public WeatherProperties setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public WeatherProperties setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }
}
