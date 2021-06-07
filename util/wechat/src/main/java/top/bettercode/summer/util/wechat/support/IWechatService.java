package top.bettercode.summer.util.wechat.support;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Peter Wu
 */
public interface IWechatService {

  @Transactional
  String oauth(String openId);
}
