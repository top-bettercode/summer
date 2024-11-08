package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcInvestorPositionDetailField

/**
 * @author Peter Wu
 */
data class InvestorPositionDetail(
    var reserve1: String? = null,
    var brokerID: String? = null,
    var investorID: String? = null,
    var hedgeFlag: Char = 0.toChar(),
    var direction: Char = 0.toChar(),
    var openDate: String? = null,
    var tradeID: String? = null,
    var volume: Int = 0,
    var openPrice: Double = 0.0,
    var tradingDay: String? = null,
    var settlementID: Int = 0,
    var tradeType: Char = 0.toChar(),
    var reserve2: String? = null,
    var exchangeID: String? = null,
    var closeProfitByDate: Double = 0.0,
    var closeProfitByTrade: Double = 0.0,
    var positionProfitByDate: Double = 0.0,
    var positionProfitByTrade: Double = 0.0,
    var margin: Double = 0.0,
    var exchMargin: Double = 0.0,
    var marginRateByMoney: Double = 0.0,
    var marginRateByVolume: Double = 0.0,
    var lastSettlementPrice: Double = 0.0,
    var settlementPrice: Double = 0.0,
    var closeVolume: Int = 0,
    var closeAmount: Double = 0.0,
    var timeFirstVolume: Int = 0,
    var investUnitID: String? = null,
    var specPosiType: Char = 0.toChar(),
    var instrumentID: String? = null,
    var combInstrumentID: String? = null,
) {
    companion object {
        fun from(field: CThostFtdcInvestorPositionDetailField): InvestorPositionDetail {
            val obj = InvestorPositionDetail()
            obj.reserve1 = field.reserve1
            obj.brokerID = field.brokerID
            obj.investorID = field.investorID
            obj.hedgeFlag = field.hedgeFlag
            obj.direction = field.direction
            obj.openDate = field.openDate
            obj.tradeID = field.tradeID
            obj.volume = field.volume
            obj.openPrice = field.openPrice
            obj.tradingDay = field.tradingDay
            obj.settlementID = field.settlementID
            obj.tradeType = field.tradeType
            obj.reserve2 = field.reserve2
            obj.exchangeID = field.exchangeID
            obj.closeProfitByDate = field.closeProfitByDate
            obj.closeProfitByTrade = field.closeProfitByTrade
            obj.positionProfitByDate = field.positionProfitByDate
            obj.positionProfitByTrade = field.positionProfitByTrade
            obj.margin = field.margin
            obj.exchMargin = field.exchMargin
            obj.marginRateByMoney = field.marginRateByMoney
            obj.marginRateByVolume = field.marginRateByVolume
            obj.lastSettlementPrice = field.lastSettlementPrice
            obj.settlementPrice = field.settlementPrice
            obj.closeVolume = field.closeVolume
            obj.closeAmount = field.closeAmount
            obj.timeFirstVolume = field.timeFirstVolume
            obj.investUnitID = field.investUnitID
            obj.specPosiType = field.specPosiType
            obj.instrumentID = field.instrumentID
            obj.combInstrumentID = field.combInstrumentID
            return obj
        }
    }
}

