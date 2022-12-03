package top.bettercode.summer.tools.weixin.config;

import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.weixin.support.WechatToken;

/**
 * @author Peter Wu
 */
public interface IOffiaccountProperties extends IWexinProperties {

  String OAUTH_URL = "/wechat/oauth";

  default Boolean getUserUnionid() {
    return false;
  }

  String getAppBaseUrl();


  String getWechatBaseUrl();


  String getWechatWebOauthUrl();


  default String getOauthUrl() {
    return getAppBaseUrl() + OAUTH_URL;
  }

  default String redirectUrl(WechatToken wechatToken, boolean forceLogin, String state) {
    return "redirect:" + wechatUrl(wechatToken, forceLogin, state);
  }

  default String wechatUrl(WechatToken wechatToken, boolean forceLogin, String state) {
    String token = wechatToken == null ? "" : wechatToken.getAccessToken();
    String openId = wechatToken == null ? "" : wechatToken.getOpenId();
    return getWechatBaseUrl() + getWechatWebOauthUrl() + "?access_token=" + token + "&"
        + OPEN_ID_NAME + "=" + openId + "&hasBound=" + (StringUtils.hasText(token)) + "&forceLogin="
        + forceLogin + "&state=" + (state == null ? "" : state) + "&_timer="
        + System.currentTimeMillis();
  }


}
