package top.bettercode.sms.b2m;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import top.bettercode.summer.util.test.BaseTest;

/**
 * @author Peter Wu
 */
class B2mSmsTemplateTest extends BaseTest {

  @Autowired
  B2mSmsTemplate b2mTemplate;

  @org.junit.jupiter.api.Test
  void sendPersonalitySMS() {
    b2mTemplate.sendPersonalitySMS(Collections.singletonMap("18224060100","【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"));
  }

  @Test
  void sendPersonalityAllSMS() {
    b2mTemplate.sendPersonalityAllSMS(Collections.singletonMap("18224060100","【云图复肥】客户您好，2020年09月22日您的订单在应城市新都化工有限责任公司复合肥工厂12已发货物料：45%241407新氨化掺混腐植酸中氯黑， 数量：12.700，单位：吨，请注意查收，如有异议请在48小时内联系热线028-83950223。"));
  }
}