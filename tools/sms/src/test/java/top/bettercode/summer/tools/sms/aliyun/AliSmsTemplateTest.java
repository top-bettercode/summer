package top.bettercode.summer.tools.sms.aliyun;


import kotlin.Pair;
import kotlin.collections.MapsKt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import top.bettercode.summer.test.BaseTest;

/**
 * @author Peter Wu
 */
@Disabled
class AliSmsTemplateTest extends BaseTest {

  @Autowired
  AliSmsTemplate aliSmsTemplate;
  @Value("${mobile}")
  String mobile;

  @Disabled
  @Test
  void sendSms() {
    aliSmsTemplate.sendSms("SMS_126359654", new AliSmsReq(mobile, "云图提货", MapsKt.mapOf(
        new Pair<>("code", "123456"))));
  }

  @Test
  void querySendReport() {
    aliSmsTemplate.querySendReport(mobile);
  }

  @Test
  void querySmsSignList() {
    aliSmsTemplate.querySmsSignList();
  }

  @Test
  void querySmsTemplateList() {
    aliSmsTemplate.querySmsTemplateList();
  }

}