package top.bettercode.summer.tools.ctp.mdapi;

import ctp.thostmduserapi.CThostFtdcDepthMarketDataField;
import ctp.thostmduserapi.CThostFtdcMdApi;
import ctp.thostmduserapi.CThostFtdcMdSpi;
import ctp.thostmduserapi.CThostFtdcReqUserLoginField;
import ctp.thostmduserapi.CThostFtdcRspInfoField;
import ctp.thostmduserapi.CThostFtdcRspUserLoginField;
import ctp.thostmduserapi.CThostFtdcSpecificInstrumentField;
import ctp.thostmduserapi.CThostFtdcUserLogoutField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bettercode.summer.tools.ctp.mdapi.config.MdapiProperties;
import top.bettercode.summer.tools.lang.util.StringUtil;

public class MdspiImpl extends CThostFtdcMdSpi {

  private final Logger log = LoggerFactory.getLogger(MdspiImpl.class);
  private final CThostFtdcMdApi mdapi;
  private final MdapiProperties properties;
  private boolean userLogin;

  public MdspiImpl(MdapiProperties properties, CThostFtdcMdApi mdapi) {
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
    userLogin = true;
  }

  @Override
  public void OnRspUserLogout(CThostFtdcUserLogoutField cThostFtdcUserLogoutField,
      CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
    log.info("用户登出");
    super.OnRspUserLogout(cThostFtdcUserLogoutField, cThostFtdcRspInfoField, i, b);
  }

  @Override
  public void OnRspError(CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
    log.info("rspInfo:\n{}", StringUtil.json(cThostFtdcRspInfoField, true));
    super.OnRspError(cThostFtdcRspInfoField, i, b);
  }

  @Override
  public void OnRspSubMarketData(
      CThostFtdcSpecificInstrumentField cThostFtdcSpecificInstrumentField,
      CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
    log.info("订阅行情：\n{}\n\n{}"
        , StringUtil.json(cThostFtdcSpecificInstrumentField, true)
        , StringUtil.json(cThostFtdcRspInfoField, true));
    super.OnRspSubMarketData(cThostFtdcSpecificInstrumentField, cThostFtdcRspInfoField, i, b);
  }

  @Override
  public void OnRspUnSubMarketData(
      CThostFtdcSpecificInstrumentField cThostFtdcSpecificInstrumentField,
      CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
    log.info("退订行情：\n{}\n\n{}"
        , StringUtil.json(cThostFtdcSpecificInstrumentField, true)
        , StringUtil.json(cThostFtdcRspInfoField, true));

    super.OnRspUnSubMarketData(cThostFtdcSpecificInstrumentField, cThostFtdcRspInfoField, i, b);
  }

  @Override
  public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
    log.info("marketData:\n{}\n最新单价:{} 涨幅：{} 成交量:{}",
        StringUtil.json(pDepthMarketData, true),
        pDepthMarketData.getLastPrice(),
        (pDepthMarketData.getLastPrice() - pDepthMarketData.getPreClosePrice()) * 100
            / pDepthMarketData.getPreClosePrice(),
        pDepthMarketData.getVolume()
    );
  }

}
