package top.bettercode.summer.tools.ctp;

import ctp.thostapi.CThostFtdcMdApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import top.bettercode.summer.test.BaseWebNoAuthTest;
import top.bettercode.summer.tools.ctp.config.CtpProperties;

/**
 * @author Peter Wu
 */
//@Disabled
class MdspiImplTest extends BaseWebNoAuthTest {

  @Autowired
  CtpProperties properties;

  /**
   * <p>
   * 另外，有以下注意事项，请您参考： 注意事项：
   * <p>
   * 1.仿真测试使用API版本为评测版，请在测试时替换API为此版本，必要时需重新编译
   * <p>
   * 2.仿真测试认证码于第二天生效，仅用做测试使用，请勿用于实盘
   * <p>
   * 3.仿真测试环境白天开放时间与实盘交易时间一致，无夜盘，请合理安排测试时间
   * <p>
   * 4.仿真测试环境行情与实盘行情相互独立，行情不活跃，建议在测试时收取中金所合约行情，下单中金所合约
   * <p>
   * 5. 99683265公共测试账号仅用于看穿式监管功能测试，如您需要在仿真环境进行策略验证，建议在我司申请仿真测试账号，以免对您的策略验证产生干扰 我司仿真测试账号申请地址： <a
   * href="http://qh.newone.com.cn/main/personal_business/emulation_trade/fzkh/index.shtml">...</a>
   * <p>
   * 6.实盘使用API版本为正式版，请您在对接实盘前替换API为此版本，必要时需重新编译
   */
  @Test
  void test() throws Exception {
    CThostFtdcMdApi mdApi = CThostFtdcMdApi.CreateFtdcMdApi("build/", properties.isUdp());
    MdspiImpl pMdspiImpl = new MdspiImpl(properties, mdApi);
    mdApi.RegisterSpi(pMdspiImpl);
    mdApi.RegisterFront(properties.getMdRegisterFront());
    mdApi.Init();
    while (!pMdspiImpl.isUserLogin()) {
      Thread.sleep(1000);
    }
    String[] instruementid = new String[1];
    //订阅合约号修改这里，如果运行成功没收到行情，参见如下解决
    //https://blog.csdn.net/pjjing/article/details/100532276
    instruementid[0] = "cu2304";
    mdApi.SubscribeMarketData(instruementid, 1);

    mdApi.Join();
  }
}