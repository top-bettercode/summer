package top.bettercode.sms.aliyun;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import top.bettercode.summer.util.test.BaseTest;

/**
 * @author Peter Wu
 */
class AliSmsTemplateTest extends BaseTest {

  @Autowired
  AliSmsTemplate aliSmsTemplate;

  @Test
  void sendSms() {
    aliSmsTemplate.sendSms("SMS_126359654", new AliSmsReq("18224060100","云图提货", ImmutableMap.of("code","123456")));
  }

  @Test
  void querySendDetails(){
    aliSmsTemplate.querySendDetails("18224060100");
  }
}