package cn.bestwu.summer.util.wechat.controller;

import cn.bestwu.logging.annotation.RequestLogging;
import cn.bestwu.simpleframework.web.BaseController;
import cn.bestwu.summer.util.wechat.support.IWechatService;
import cn.bestwu.summer.util.wechat.config.WechatProperties;
import com.riversoft.weixin.common.decrypt.AesException;
import com.riversoft.weixin.common.decrypt.SHA1;
import com.riversoft.weixin.common.jsapi.JsAPISignature;
import com.riversoft.weixin.common.oauth2.AccessToken;
import com.riversoft.weixin.mp.jsapi.JsAPIs;
import com.riversoft.weixin.mp.oauth2.MpOAuth2s;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@ConditionalOnWebApplication
@Controller
@RequestMapping(value = "/wechat", name = "微信")
public class WechatCallbackController extends BaseController {

  private final WechatProperties wechatProperties;
  private final IWechatService wechatService;

  public WechatCallbackController(IWechatService wechatService, WechatProperties wechatProperties) {
    this.wechatService = wechatService;
    this.wechatProperties = wechatProperties;
  }

  /**
   * 公众号OAuth回调接口
   *
   * @param state state
   */
  @RequestLogging(ignoredTimeout = true)
  @GetMapping(value = "/oauth", name = "OAuth回调接口")
  public String oauth(String code, String state) {
    log.debug("code:{}, state:{}", code, state);
    plainTextError();
    String openId = null;
    String token = null;
    try {
      MpOAuth2s oAuth2s = MpOAuth2s.with(wechatProperties);
      AccessToken accessToken = oAuth2s.getAccessToken(code);
      openId = accessToken.getOpenId();
      log.info("openId:{}", openId);
      token = wechatService.oauth(openId);
    } catch (Exception e) {
      log.warn("token获取失败", e);
    }
    return wechatProperties.redirectUrl(token, openId, token != null);
  }

  /**
   * js签名
   */
  @ResponseBody
  @GetMapping(value = "/jsSign", name = "js签名")
  public Object sign(String url) {
    JsAPISignature jsAPISignature = JsAPIs.with(wechatProperties).createJsAPISignature(url);
    return ok(jsAPISignature);
  }

  @ResponseBody
  @GetMapping(name = "验证回调")
  public Object access(String signature, String echostr, String timestamp, String nonce)
      throws AesException {
    log.debug("signature={}, timestamp={}, nonce={}, echostr={}", signature, timestamp, nonce,
        echostr);
    if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !SHA1
        .getSHA1(wechatProperties.getToken(), timestamp, nonce)
        .equals(signature)) {
      log.warn("非法请求.");
      return false;
    }
    return echostr;
  }

//  @ResponseBody
//  @PostMapping(name = "事件推送")
//  public String receive(String signature, String timestamp,
//      String nonce, String openid, String encrypt_type, String msg_signature,
//      @RequestBody String content) {
//    wechatService.receive(timestamp, nonce, openid, encrypt_type, msg_signature, content);
//    return null;
//  }


}
