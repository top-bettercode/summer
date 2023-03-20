package top.bettercode.summer.tools.ctp.mdapi;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 */
public class NativeLibTest {

  @Test
  void test() throws Exception{
    System.load("/data/repositories/bettercode/default/summer/tools/ctp-mdapi/native/cp/libthostmduserapi_se.so");
    System.load("/data/repositories/bettercode/default/summer/tools/ctp-mdapi/native/cp/libthostmduserapi_wrap.so");

  }
}
