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

    /**
     * <pre>
     *     投资者最新的资金状况：
     * 1true
     *
     * {
     *   "brokerID" : "8060",
     *   "preMortgage" : 0.0,
     *   "exchangeDeliveryMargin" : 265866.0,
     *   "withdrawQuota" : 3.1404797062650007E8,
     *   "reserve" : 10.0,
     *   "settlementID" : 1,
     *   "credit" : 0.0,
     *   "mortgage" : 0.0,
     *   "exchangeMargin" : 6918907.330000007,
     *   "deliveryMargin" : 345625.8,
     *   "reserveBalance" : 0.0,
     *   "preFundMortgageIn" : 0.0,
     *   "preFundMortgageOut" : 0.0,
     *   "fundMortgageIn" : 0.0,
     *   "fundMortgageOut" : 0.0,
     *   "fundMortgageAvailable" : 0.0,
     *   "mortgageableFund" : 3.4894217958500004E8,
     *   "specProductMargin" : 353490.94999999995,
     *   "specProductFrozenMargin" : 0.0,
     *   "specProductCommission" : 0.0,
     *   "specProductFrozenCommission" : 0.0,
     *   "specProductPositionProfit" : -16930.0,
     *   "specProductCloseProfit" : 0.0,
     *   "specProductPositionProfitByAlg" : 0.0,
     *   "specProductExchangeMargin" : 298442.5,
     *   "frozenSwap" : 0.0,
     *   "remainSwap" : 0.0,
     *   "preCredit" : 0.0,
     *   "preDeposit" : 3.5718044190500003E8,
     *   "preBalance" : 3.5718044190500003E8,
     *   "preMargin" : 8238252.32,
     *   "interestBase" : 0.0,
     *   "interest" : 0.0,
     *   "deposit" : 0.0,
     *   "withdraw" : 0.0,
     *   "frozenMargin" : 0.0,
     *   "frozenCash" : 0.0,
     *   "currMargin" : 7892626.519999993,
     *   "cashIn" : 0.0,
     *   "commission" : 0.0,
     *   "closeProfit" : 0.0,
     *   "positionProfit" : 269044.9999999972,
     *   "balance" : 3.5744948690500003E8,
     *   "available" : 3.4894218958500004E8,
     *   "tradingDay" : "20230330",
     *   "frozenCommission" : 0.0,
     *   "currencyID" : "CNY",
     *   "bizType" : "\u0000",
     *   "accountID" : "99683265"
     * }
     * </pre>
     * @param pTradingAccount
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    @Override
    public void OnRspQryTradingAccount(CThostFtdcTradingAccountField pTradingAccount,
                                       CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        log.info("投资者最新的资金状况：\n{}{}\n\n{}\n\n{}", nRequestID, bIsLast
                , StringUtil.json(pTradingAccount, true)
                , StringUtil.json(pRspInfo, true));
    }

    /**
     * <pre>
     *     查询持仓（汇总）：\n0,false
     * {
     *   "brokerID" : "8060",
     *   "reserve1" : "SP m2309&m2309",
     *   "posiDirection" : "2",
     *   "longFrozenAmount" : 0.0,
     *   "shortFrozen" : 0,
     *   "shortFrozenAmount" : 0.0,
     *   "openVolume" : 0,
     *   "closeVolume" : 0,
     *   "openAmount" : 0.0,
     *   "closeAmount" : 0.0,
     *   "positionCost" : 0.0,
     *   "preMargin" : 0.0,
     *   "useMargin" : 8673.6,
     *   "frozenMargin" : 0.0,
     *   "frozenCash" : 0.0,
     *   "frozenCommission" : 0.0,
     *   "cashIn" : 0.0,
     *   "commission" : 0.0,
     *   "closeProfit" : 0.0,
     *   "positionProfit" : 0.0,
     *   "preSettlementPrice" : 0.0,
     *   "settlementPrice" : 0.0,
     *   "settlementID" : 1,
     *   "openCost" : 0.0,
     *   "exchangeMargin" : 7228.000000000001,
     *   "combPosition" : 0,
     *   "combLongFrozen" : 0,
     *   "combShortFrozen" : 0,
     *   "closeProfitByDate" : 0.0,
     *   "closeProfitByTrade" : 0.0,
     *   "todayPosition" : 0,
     *   "marginRateByMoney" : 0.0,
     *   "marginRateByVolume" : 0.0,
     *   "strikeFrozen" : 0,
     *   "strikeFrozenAmount" : 0.0,
     *   "abandonFrozen" : 0,
     *   "ydStrikeFrozen" : 0,
     *   "positionCostOffset" : 0.0,
     *   "tasPosition" : 0,
     *   "tasPositionCost" : 0.0,
     *   "tradingDay" : "20230330",
     *   "hedgeFlag" : "1",
     *   "positionDate" : "1",
     *   "ydPosition" : 2,
     *   "longFrozen" : 0,
     *   "position" : 2,
     *   "investorID" : "99683265",
     *   "instrumentID" : "SP m2309&m2309",
     *   "exchangeID" : "DCE",
     *   "investUnitID" : ""
     * }
     *
     * </pre>
     * @param cThostFtdcInvestorPositionField
     * @param cThostFtdcRspInfoField
     * @param i
     * @param b
     */
    @Override
    public void OnRspQryInvestorPosition(
            CThostFtdcInvestorPositionField cThostFtdcInvestorPositionField,
            CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("查询持仓（汇总）：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInvestorPositionField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));

    }

    /**
     * <pre>
     *     投机套保类型增加枚举值：
     * ///TFtdcHedgeFlagType 是一个投机套保标志类型
     * ///投机
     * #define THOST_FTDC_HF_Speculation '1'
     * ///套利
     * #define THOST_FTDC_HF_Arbitrage '2'
     * ///套保
     * #define THOST_FTDC_HF_Hedge '3'
     * ///做市商
     * #define THOST_FTDC_HF_MarketMaker '5'
     * ///第一腿投机第二腿套保 大商所专用
     * #define THOST_FTDC_HF_SpecHedge '6'
     * ///第一腿套保第二腿投机 大商所专用
     * #define THOST_FTDC_HF_HedgeSpec '7'
     *
     * </pre>
     * <pre>
     *     查询持仓明细：\n0,false
     * {
     *   "brokerID" : "8060",
     *   "reserve1" : "si2310",
     *   "direction" : "1",
     *   "openDate" : "20221207",
     *   "tradeID" : "    60003580",
     *   "volume" : 1,
     *   "openPrice" : 17880.0,
     *   "settlementID" : 1,
     *   "tradeType" : "0",
     *   "closeProfitByTrade" : 0.0,
     *   "positionProfitByDate" : 0.0,
     *   "positionProfitByTrade" : -53175.0,
     *   "margin" : 18534.75,
     *   "exchMargin" : 11406.000000000002,
     *   "marginRateByMoney" : 0.13,
     *   "marginRateByVolume" : 0.0,
     *   "lastSettlementPrice" : 28515.0,
     *   "settlementPrice" : 28515.0,
     *   "closeVolume" : 0,
     *   "closeAmount" : 0.0,
     *   "timeFirstVolume" : 1,
     *   "specPosiType" : "#",
     *   "combInstrumentID" : "",
     *   "hedgeFlag" : "1",
     *   "tradingDay" : "20230330",
     *   "closeProfitByDate" : 0.0,
     *   "investorID" : "99683265",
     *   "instrumentID" : "si2310",
     *   "exchangeID" : "GFEX",
     *   "reserve2" : "",
     *   "investUnitID" : ""
     * }
     * </pre>
     * @param cThostFtdcInvestorPositionDetailField
     * @param cThostFtdcRspInfoField
     * @param i
     * @param b
     */
    @Override
    public void OnRspQryInvestorPositionDetail(CThostFtdcInvestorPositionDetailField cThostFtdcInvestorPositionDetailField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("查询持仓明细：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInvestorPositionDetailField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));
    }

    /**
     * <pre>
     *      查询组合持仓明细：\n0,false
     * {
     *   "brokerID" : "8060",
     *   "reserve1" : "a2309",
     *   "openDate" : "20230314",
     *   "marginRateByMoney" : 0.0,
     *   "settlementID" : 1,
     *   "comTradeID" : "2023033000000642",
     *   "tradeID" : "   100007911",
     *   "hedgeFlag" : "1",
     *   "direction" : "0",
     *   "totalAmt" : 1,
     *   "margin" : 0.0,
     *   "exchMargin" : 0.0,
     *   "marginRateByVolume" : 0.0,
     *   "legID" : 1,
     *   "legMultiple" : 1,
     *   "tradeGroupID" : 221,
     *   "tradingDay" : "20230330",
     *   "investorID" : "99683265",
     *   "instrumentID" : "a2309",
     *   "exchangeID" : "DCE",
     *   "investUnitID" : "",
     *   "combInstrumentID" : "SP a2307&a2309",
     *   "reserve2" : "SP a2307&a2309"
     * }
     * </pre>
     * @param cThostFtdcInvestorPositionCombineDetailField
     * @param cThostFtdcRspInfoField
     * @param i
     * @param b
     */
    @Override
    public void OnRspQryInvestorPositionCombineDetail(CThostFtdcInvestorPositionCombineDetailField cThostFtdcInvestorPositionCombineDetailField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        log.info("查询组合持仓明细：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInvestorPositionCombineDetailField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));
    }

    /**
     * <pre>
     *     查询合约：\n0,true
     * {
     *   "instrumentName" : "中证商品指数2303",
     *   "reserve1" : "",
     *   "maxMarketOrderVolume" : 100,
     *   "productClass" : "1",
     *   "positionType" : "2",
     *   "positionDateType" : "2",
     *   "longMarginRatio" : 0.05,
     *   "shortMarginRatio" : 0.05,
     *   "maxMarginSideAlgorithm" : "0",
     *   "reserve4" : "",
     *   "strikePrice" : 0.0,
     *   "optionsType" : "\u0000",
     *   "underlyingMultiple" : 0.0,
     *   "combinationType" : "0",
     *   "underlyingInstrID" : "",
     *   "deliveryYear" : 2023,
     *   "deliveryMonth" : 3,
     *   "minMarketOrderVolume" : 1,
     *   "maxLimitOrderVolume" : 100,
     *   "minLimitOrderVolume" : 1,
     *   "volumeMultiple" : 100,
     *   "priceTick" : 0.05,
     *   "createDate" : "20220811",
     *   "openDate" : "20220811",
     *   "expireDate" : "20230330",
     *   "startDelivDate" : "",
     *   "endDelivDate" : "20230330",
     *   "instLifePhase" : "1",
     *   "isTrading" : 1,
     *   "instrumentID" : "ci2303",
     *   "exchangeID" : "GFEX",
     *   "reserve2" : "",
     *   "reserve3" : "",
     *   "exchangeInstID" : "ci2303",
     *   "productID" : "ci"
     * }
     * </pre>
     * @param cThostFtdcInstrumentField
     * @param cThostFtdcRspInfoField
     * @param i
     * @param b
     */
    @Override
    public void OnRspQryInstrument(CThostFtdcInstrumentField cThostFtdcInstrumentField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {

        log.info("查询合约：\\n{},{}\n{}\n\n{}\n", i, b
                , StringUtil.json(cThostFtdcInstrumentField, true)
                , StringUtil.json(cThostFtdcRspInfoField, true));
    }
}
