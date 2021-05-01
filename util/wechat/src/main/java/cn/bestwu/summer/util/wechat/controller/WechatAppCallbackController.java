package cn.bestwu.summer.util.wechat.controller;

import cn.bestwu.logging.annotation.RequestLogging;
import cn.bestwu.simpleframework.web.BaseController;
import cn.bestwu.summer.util.wechat.config.WechatMiniAppProperties;
import cn.bestwu.summer.util.wechat.support.IWechatService;
import com.riversoft.weixin.app.user.SessionKey;
import com.riversoft.weixin.app.user.Users;
import com.riversoft.weixin.common.exception.WxRuntimeException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@ConditionalOnWebApplication
@Controller
@RequestMapping(value = "/wechat", name = "微信")
public class WechatAppCallbackController extends BaseController {

  private final WechatMiniAppProperties wechatMiniAppProperties;
  private final IWechatService wechatService;

  public WechatAppCallbackController(IWechatService wechatService,
      WechatMiniAppProperties wechatMiniAppProperties) {
    this.wechatService = wechatService;
    this.wechatMiniAppProperties = wechatMiniAppProperties;
  }

  @RequestLogging(ignoredTimeout = true)
  @ResponseBody
  @Transactional
  @PostMapping(value = "/miniOauth", name = "小程序code2Session授权接口")
  public Object miniOauth(String code) {
    log.debug("code:{}", code);
    try {
      Users users = Users.with(wechatMiniAppProperties);
      String openId;
      SessionKey sessionKey = users.code2Session(code);
      openId = sessionKey.getOpenId();
      log.info("openId:{}", openId);
      String token = wechatService.oauth(openId);
      Map<String, Object> result = new HashMap<>();
      result.put("access_token", token);
      result.put(WechatMiniAppProperties.OPEN_ID_FIELD_NAME, openId);
      result.put("hasBound", token != null);
      return ok(result);
    } catch (WxRuntimeException e) {
      log.warn("授权失败," + e.getWxError().getErrorMsg(), e);
      Map<String, Object> result = new HashMap<>();
      result.put("access_token", null);
      result.put(WechatMiniAppProperties.OPEN_ID_FIELD_NAME, null);
      result.put("hasBound", false);
      return ok(result);
    } catch (Exception e) {
      log.error("授权失败", e);
      Map<String, Object> result = new HashMap<>();
      result.put("access_token", null);
      result.put(WechatMiniAppProperties.OPEN_ID_FIELD_NAME, null);
      result.put("hasBound", false);
      return ok(result);
    }
  }

}
