package top.bettercode.sms.b2m;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import top.bettercode.summer.util.test.BaseTest;

/**
 * @author Peter Wu
 */
class B2mSmsTemplateTest extends BaseTest {

  @Autowired
  B2mSmsTemplate b2mTemplate;
  @Autowired
  SimpleB2mSmsTemplate simpleB2mTemplate;
  @Value("${mobile}")
  String mobile;

  @Disabled
  @Test
  void simpleSendSms() {
    simpleB2mTemplate.simpleSendSms(Collections.singletonMap(mobile,
        "【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"));
  }

  @Disabled
  @Test
  void sendSms() {
    b2mTemplate.sendSms(Collections.singletonMap(mobile,
        "【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"));
  }

  @Test
  void simpleQuerySendReport() {
    simpleB2mTemplate.simpleQuerySendReport();
  }

  @Test
  void querySendReport() {
    b2mTemplate.querySendReport();
  }

  @Disabled
  @Test
  void retrieveReport() {
    simpleB2mTemplate.retrieveReport(LocalDateTime.of(2022, 3, 30, 9,0));
  }
}