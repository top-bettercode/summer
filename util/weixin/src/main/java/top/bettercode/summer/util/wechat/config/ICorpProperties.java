package top.bettercode.summer.util.wechat.config;

import org.springframework.util.StringUtils;
import top.bettercode.summer.util.wechat.support.WechatToken;

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


  default String redirectUrl(WechatToken wechatToken, boolean forceLogin) {
    return "redirect:" + wechatUrl(wechatToken, forceLogin);
  }

  default String wechatUrl(WechatToken wechatToken, boolean forceLogin) {
    String token = wechatToken == null ? "" : wechatToken.getAccessToken();
    String openId = wechatToken == null ? "" : wechatToken.getOpenId();
    return getWechatBaseUrl() + getWechatWebOauthUrl() + "?access_token=" + token + "&"
        + OPEN_ID_NAME + "=" + openId + "&hasBound=" + (StringUtils.hasText(token)) + "&forceLogin="
        + forceLogin + "&_timer=" + System.currentTimeMillis();
  }

}
