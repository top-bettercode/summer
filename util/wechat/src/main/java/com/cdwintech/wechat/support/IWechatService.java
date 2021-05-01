package com.cdwintech.wechat.support;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Peter Wu
 */
public interface IWechatService {

  @Async
  void receive(String timestamp, String nonce, String openid, String encrypt_type,
      String msg_signature, @RequestBody String content);

  @Transactional
  String oauth(String openId);
}
