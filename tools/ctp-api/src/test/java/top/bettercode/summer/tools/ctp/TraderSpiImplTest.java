package top.bettercode.summer.tools.ctp;

import ctp.thostapi.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import top.bettercode.summer.test.BaseWebNoAuthTest;
import top.bettercode.summer.tools.ctp.config.CtpProperties;

/**
 * @author Peter Wu
 */
class TraderSpiImplTest extends BaseWebNoAuthTest {

    @Autowired
    CtpProperties properties;

    @Test
    void test() throws Exception {
        CThostFtdcTraderApi traderApi = CThostFtdcTraderApi.CreateFtdcTraderApi("build/");
        TraderSpiImpl traderSpi = new TraderSpiImpl(properties, traderApi);
        traderApi.RegisterSpi(traderSpi);
        traderApi.RegisterFront(properties.getTraderRegisterFront());
        traderApi.SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
        traderApi.SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
        traderApi.Init();

        while (!traderSpi.isUserLogin()) {
            Thread.sleep(1000);
        }

        //    查询持仓（汇总）
        CThostFtdcQryInvestorPositionField qryInvestorPositionField = new CThostFtdcQryInvestorPositionField();
//        traderApi.ReqQryInvestorPosition(qryInvestorPositionField, 0);
//    查询持仓明细
        CThostFtdcQryInvestorPositionDetailField field = new CThostFtdcQryInvestorPositionDetailField();
//    field.setBrokerID(properties.getBrokerId());
//    field.setInvestorID(properties.getUserId());
//    field.setInstrumentID("SP a2307&a2309");
//        traderApi.ReqQryInvestorPositionDetail(field, 0);

        CThostFtdcQryInvestorPositionCombineDetailField combineDetailField=new CThostFtdcQryInvestorPositionCombineDetailField();
        traderApi.ReqQryInvestorPositionCombineDetail(combineDetailField,0);

        //资金账户
        CThostFtdcQryTradingAccountField qryTradingAccount = new CThostFtdcQryTradingAccountField();
        qryTradingAccount.setBrokerID(properties.getBrokerId());
        qryTradingAccount.setCurrencyID(properties.getCurrencyId());

        qryTradingAccount.setInvestorID(properties.getUserId());
//        traderApi.ReqQryTradingAccount(qryTradingAccount, 1);

        CThostFtdcQryInstrumentField ftdcQryInstrumentField = new CThostFtdcQryInstrumentField();
        ftdcQryInstrumentField.setInstrumentID("ci2303");
        ftdcQryInstrumentField.setExchangeID("GFEX");
//        ftdcQryInstrumentField.setExchangeInstID("");
//        ftdcQryInstrumentField.setProductID("");
//        traderApi.ReqQryInstrument(ftdcQryInstrumentField,0);

        traderApi.Join();

    }
}