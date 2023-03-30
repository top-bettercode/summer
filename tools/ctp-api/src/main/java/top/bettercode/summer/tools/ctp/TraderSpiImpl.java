package top.bettercode.summer.tools.ctp;

import ctp.thostapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.tools.ctp.config.CtpProperties;
import top.bettercode.summer.tools.lang.util.StringUtil;

public class TraderSpiImpl extends CThostFtdcTraderSpi {

    private final Logger log = LoggerFactory.getLogger(TraderSpiImpl.class);

    private final CtpProperties properties;
    private CThostFtdcTraderApi traderApi;

    private boolean userLogin;

    public TraderSpiImpl(CtpProperties properties, CThostFtdcTraderApi traderapi) {
        this.properties = properties;
        traderApi = traderapi;
    }

    public boolean isUserLogin() {
        return userLogin;
    }

    @Override
    public void OnFrontConnected() {
        log.info("CTP连接建立");
        CThostFtdcReqAuthenticateField field = new CThostFtdcReqAuthenticateField();
        field.setBrokerID(properties.getBrokerId());
        field.setUserID(properties.getUserId());
        field.setAppID(properties.getAppId());
        field.setAuthCode(properties.getAuthCode());
        traderApi.ReqAuthenticate(field, 0);
    }

    @Override
    public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField,
                                  CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        log.info("requestId:{}\nisLast:{}\nuserLogin:\n{}\n\ninfo:\n{}",
                nRequestID, bIsLast,
                StringUtil.json(pRspAuthenticateField, true),
                StringUtil.json(pRspInfo, true));

        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            return;
        }
        CThostFtdcReqUserLoginField field = new CThostFtdcReqUserLoginField();
        field.setBrokerID(properties.getBrokerId());
        field.setUserID(properties.getUserId());
        field.setPassword(properties.getPassword());
        traderApi.ReqUserLogin(field, 0);
    }

    @Override
    public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin,
                               CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        log.info("requestId:{}\nisLast:{}\nuserLogin:\n{}\n\ninfo:\n{}",
                nRequestID, bIsLast,
                StringUtil.json(pRspUserLogin, true),
                StringUtil.json(pRspInfo, true));

        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            return;
        }
        userLogin = true;
    }

    @Override
    public void OnRspUserLogout(CThostFtdcUserLogoutField cThostFtdcUserLogoutField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("用户登出");
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("rspInfo:\n{}", StringUtil.json(cThostFtdcRspInfoField, true));
    }

    @Override
    public void OnRspQryTradingAccount(CThostFtdcTradingAccountField pTradingAccount,
                                       CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        log.info("投资者最新的资金状况：\n{}{}\n\n{}\n\n{}", nRequestID, bIsLast
                , StringUtil.json(pTradingAccount, true)
                , StringUtil.json(pRspInfo, true));
    }


    @Override
    public void OnRspQryInvestorPosition(
            CThostFtdcInvestorPositionField cThostFtdcInvestorPositionField,
            CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("查询持仓（汇总）：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInvestorPositionField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));

    }

    @Override
    public void OnRspQryInvestorPositionDetail(CThostFtdcInvestorPositionDetailField cThostFtdcInvestorPositionDetailField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("查询持仓明细：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInvestorPositionDetailField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));
    }
}
