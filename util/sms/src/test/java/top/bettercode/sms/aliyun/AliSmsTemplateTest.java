package top.bettercode.sms.aliyun;


import kotlin.Pair;
import kotlin.collections.MapsKt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import top.bettercode.summer.util.test.BaseTest;

/**
 * @author Peter Wu
 */
class AliSmsTemplateTest extends BaseTest {

  @Autowired
  AliSmsTemplate aliSmsTemplate;
  @Value("${mobile}")
  String mobile;

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