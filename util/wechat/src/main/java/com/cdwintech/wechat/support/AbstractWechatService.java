package com.cdwintech.wechat.support;

import cn.bestwu.lang.util.StringUtil;
import com.riversoft.weixin.common.decrypt.AesException;
import com.riversoft.weixin.common.decrypt.MessageDecryption;
import com.riversoft.weixin.common.event.ClickEvent;
import com.riversoft.weixin.common.event.EventRequest;
import com.riversoft.weixin.common.message.XmlMessageHeader;
import com.riversoft.weixin.mp.message.MpXmlMessages;
import com.cdwintech.wechat.config.WechatProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Peter Wu
 */
public abstract class AbstractWechatService implements IWechatService {

  private final Logger log = LoggerFactory.getLogger(AbstractWechatService.class);
  private final DuplicatedMessageChecker duplicatedMessageChecker;
  private final WechatProperties wechatProperties;

  public AbstractWechatService(
      DuplicatedMessageChecker duplicatedMessageChecker,
      WechatProperties wechatProperties) {
    this.duplicatedMessageChecker = duplicatedMessageChecker;
    this.wechatProperties = wechatProperties;
  }

  @Override
  @Async
  public void receive(String timestamp, String nonce, String openid, String encrypt_type,
      String msg_signature, @RequestBody String content) {
    XmlMessageHeader xmlRequest;
    if ("aes".equals(encrypt_type)) {
      try {
        MessageDecryption messageDecryption = new MessageDecryption(wechatProperties.getToken(),
            wechatProperties.getAesKey(), wechatProperties.getAppId());
        xmlRequest = MpXmlMessages
            .fromXml(messageDecryption.decrypt(msg_signature, timestamp, nonce, content));
        mpDispatch(xmlRequest, openid);
      } catch (AesException ignored) {
      }
    } else {
      try {
        xmlRequest = MpXmlMessages.fromXml(content);
        mpDispatch(xmlRequest, openid);
      } catch (Exception ignored) {
      }
    }
  }

  private void mpDispatch(XmlMessageHeader xmlRequest, String openid) {
    if (!duplicatedMessageChecker
        .isDuplicated(xmlRequest.getFromUser() + xmlRequest.getCreateTime().getTime())) {
//      String welcome = "您好:" + Users.with(appSetting).get(xmlRequest.getFromUser()).getNickName();
//      CareMessages.with(appSetting).text(xmlRequest.getFromUser(), welcome);
      log.info("事件请求[{}]", StringUtil.valueOf(xmlRequest));

      if (xmlRequest instanceof ClickEvent) {
//        String openid = xmlRequest.getFromUser();
//        String eventKey = ((ClickEvent) xmlRequest).getEventKey();
//        CareMessages careMessages = CareMessages.with(wechatProperties);
//        switch (eventKey) {
//
//        }
      } else if (xmlRequest instanceof EventRequest) {
//        EventRequest eventRequest = (EventRequest) xmlRequest;
//        CareMessages.with(appSetting)
//            .text(xmlRequest.getFromUser(), "事件请求:" + eventRequest.getEventType().name());
      } else {
//        CareMessages.with(appSetting)
//            .text(xmlRequest.getFromUser(), "消息请求:" + xmlRequest.getMsgType().name());
      }
    } else {
      log.warn("Duplicated message: {} @ {}", xmlRequest.getMsgType(), xmlRequest.getFromUser());
    }
  }
}
