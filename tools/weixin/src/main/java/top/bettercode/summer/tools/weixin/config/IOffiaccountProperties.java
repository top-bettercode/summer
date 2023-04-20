package top.bettercode.summer.tools.weixin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.weixin.support.WechatToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Peter Wu
 */
public interface IOffiaccountProperties extends IWexinProperties {

    Logger log = LoggerFactory.getLogger(IOffiaccountProperties.class);

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
        String msg = wechatToken == null ? "" : wechatToken.getMsg();
        String encodeMsg;
        try {
            encodeMsg = URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn(e.getMessage(), e);
            encodeMsg = "";
        }
        return getWechatBaseUrl() + getWechatWebOauthUrl() + "?access_token=" + token + "&"
                + OPEN_ID_NAME + "=" + openId + "&hasBound=" + (StringUtils.hasText(token)) + "&forceLogin="
                + forceLogin + "&state=" + (state == null ? "" : state) + "&msg=" + encodeMsg + "&_timer="
                + System.currentTimeMillis();
    }


}
