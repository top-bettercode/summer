package top.bettercode.summer.util.wechat.config;

import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public interface ICorpProperties extends IWexinProperties {

  String OAUTH_URL = "/wechat/corpOauth";

  String getAppBaseUrl();


  String getWechatBaseUrl();


  String getWechatWebOauthUrl();


  default String getOauthUrl() {
    return getAppBaseUrl() + OAUTH_URL;
  }

  default String redirectUrl(String token, String openId, boolean forceLogin) {
    return "redirect:" + wechatUrl(token, openId, forceLogin);
  }

  default String wechatUrl(String token, String openId, boolean forceLogin) {
    return getWechatBaseUrl() + getWechatWebOauthUrl() + "?access_token=" + (token == null ? ""
        : token) + "&" + OPEN_ID_NAME + "=" + openId + "&hasBound=" + (StringUtils.hasText(token))
        + "&forceLogin=" + forceLogin + "&_timer="
        + System.currentTimeMillis();
  }


}
