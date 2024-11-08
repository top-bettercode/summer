package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcInvestorPositionCombineDetailField

/**
 * @author Peter Wu
 */
data class InvestorPositionCombineDetail(
    var tradingDay: String? = null,
    var openDate: String? = null,
    var exchangeID: String? = null,
    var settlementID: Int = 0,
    var brokerID: String? = null,
    var investorID: String? = null,
    var comTradeID: String? = null,
    var tradeID: String? = null,
    var reserve1: String? = null,
    var hedgeFlag: Char = 0.toChar(),
    var direction: Char = 0.toChar(),
    var totalAmt: Int = 0,
    var margin: Double = 0.0,
    var exchMargin: Double = 0.0,
    var marginRateByMoney: Double = 0.0,
    var marginRateByVolume: Double = 0.0,
    var legID: Int = 0,
    var legMultiple: Int = 0,
    var reserve2: String? = null,
    var tradeGroupID: Int = 0,
    var investUnitID: String? = null,
    var instrumentID: String? = null,
    var combInstrumentID: String? = null,
) {
    companion object {
        fun from(field: CThostFtdcInvestorPositionCombineDetailField): InvestorPositionCombineDetail {
            val obj = InvestorPositionCombineDetail()
            obj.tradingDay = field.tradingDay
            obj.openDate = field.openDate
            obj.exchangeID = field.exchangeID
            obj.settlementID = field.settlementID
            obj.brokerID = field.brokerID
            obj.investorID = field.investorID
            obj.comTradeID = field.comTradeID
            obj.tradeID = field.tradeID
            obj.reserve1 = field.reserve1
            obj.hedgeFlag = field.hedgeFlag
            obj.direction = field.direction
            obj.totalAmt = field.totalAmt
            obj.margin = field.margin
            obj.exchMargin = field.exchMargin
            obj.marginRateByMoney = field.marginRateByMoney
            obj.marginRateByVolume = field.marginRateByVolume
            obj.legID = field.legID
            obj.legMultiple = field.legMultiple
            obj.reserve2 = field.reserve2
            obj.tradeGroupID = field.tradeGroupID
            obj.investUnitID = field.investUnitID
            obj.instrumentID = field.instrumentID
            obj.combInstrumentID = field.combInstrumentID
            return obj
        }
    }
}

