package top.bettercode.summer.tools.ctp;

import ctp.thostapi.CThostFtdcDepthMarketDataField;
import ctp.thostapi.CThostFtdcMdApi;
import ctp.thostapi.CThostFtdcMdSpi;
import ctp.thostapi.CThostFtdcReqUserLoginField;
import ctp.thostapi.CThostFtdcRspInfoField;
import ctp.thostapi.CThostFtdcRspUserLoginField;
import ctp.thostapi.CThostFtdcSpecificInstrumentField;
import ctp.thostapi.CThostFtdcUserLogoutField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.tools.ctp.config.CtpProperties;
import top.bettercode.summer.tools.lang.util.StringUtil;

public class MdspiImpl extends CThostFtdcMdSpi {

    private final Logger log = LoggerFactory.getLogger(MdspiImpl.class);
    private final CThostFtdcMdApi mdapi;
    private final CtpProperties properties;
    private boolean userLogin;

    public MdspiImpl(CtpProperties properties, CThostFtdcMdApi mdapi) {
        this.properties = properties;
        this.mdapi = mdapi;
    }

    public boolean isUserLogin() {
        return userLogin;
    }

    @Override
    public void OnFrontConnected() {
        log.info("CTP连接建立");
        CThostFtdcReqUserLoginField field = new CThostFtdcReqUserLoginField();
        field.setBrokerID(properties.getBrokerId());
        field.setUserID(properties.getUserId());
        field.setPassword(properties.getPassword());
        mdapi.ReqUserLogin(field, 0);
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
    public void OnRspUserLogout(CThostFtdcUserLogoutField cThostFtdcUserLogoutField,
                                CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("用户登出");
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("rspInfo:\n{}", StringUtil.json(cThostFtdcRspInfoField, true));
    }

    @Override
    public void OnRspSubMarketData(
            CThostFtdcSpecificInstrumentField cThostFtdcSpecificInstrumentField,
            CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("订阅行情：\n{}\n\n{}"
                , StringUtil.json(cThostFtdcSpecificInstrumentField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));
    }

    @Override
    public void OnRspUnSubMarketData(
            CThostFtdcSpecificInstrumentField cThostFtdcSpecificInstrumentField,
            CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("退订行情：\n{}\n\n{}"
                , StringUtil.json(cThostFtdcSpecificInstrumentField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));

    }

    @Override
    public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
        log.info("marketData:\n{}\n最新单价:{} 涨幅：{} 成交量:{}:收盘价：{}",
                StringUtil.json(pDepthMarketData, true),
                pDepthMarketData.getLastPrice(),
                (pDepthMarketData.getLastPrice() - pDepthMarketData.getPreClosePrice()) * 100
                        / pDepthMarketData.getPreClosePrice(),
                pDepthMarketData.getVolume(),
                pDepthMarketData.getClosePrice()
        );
    }

}
