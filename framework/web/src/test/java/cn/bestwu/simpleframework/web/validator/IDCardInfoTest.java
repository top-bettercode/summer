package cn.bestwu.simpleframework.web.validator;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
public class IDCardInfoTest {

  @Test
  public void info() {
    org.junit.jupiter.api.Assertions.assertEquals("出生地：四川省宜宾市宜宾县,生日：1987年2月22日,性别：男",
        new IDCardInfo("511521198702223935").toString());
  }
}