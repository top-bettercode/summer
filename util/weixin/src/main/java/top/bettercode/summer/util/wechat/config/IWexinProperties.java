package top.bettercode.summer.util.wechat.config;

/**
 * @author Peter Wu
 */
public interface IWexinProperties {

  String OPEN_ID_NAME = "openId";

  String getAppId();

  String getSecret();

  String getToken();

  String getAesKey();

  int getConnectTimeout();

  int getReadTimeout();

  Long getCacheSeconds();

}
