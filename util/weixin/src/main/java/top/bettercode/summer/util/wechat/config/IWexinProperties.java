package top.bettercode.summer.util.wechat.config;

/**
 * @author Peter Wu
 */
public interface IWexinProperties {

  String OPEN_ID_NAME = "openId";

  default String getBasicAccessTokenUrl() {
    return "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}";
  }

  String getAppId();

  String getSecret();

  String getToken();

  String getAesKey();

  int getConnectTimeout();

  int getReadTimeout();

  Long getCacheSeconds();

  int getMaxRetries();
}
