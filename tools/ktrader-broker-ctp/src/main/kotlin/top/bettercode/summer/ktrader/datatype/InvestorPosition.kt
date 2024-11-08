package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcInvestorPositionField

/**
 * @author Peter Wu
 */
data class InvestorPosition(
    var reserve1: String? = null,
    var brokerID: String? = null,
    var investorID: String? = null,
    var posiDirection: Char = 0.toChar(),
    var hedgeFlag: Char = 0.toChar(),
    var positionDate: Char = 0.toChar(),
    var ydPosition: Int = 0,
    var longFrozen: Int = 0,
    var shortFrozen: Int = 0,
    var longFrozenAmount: Double = 0.0,
    var shortFrozenAmount: Double = 0.0,
    var openVolume: Int = 0,
    var closeVolume: Int = 0,
    var openAmount: Double = 0.0,
    var closeAmount: Double = 0.0,
    var positionCost: Double = 0.0,
    var preMargin: Double = 0.0,
    var useMargin: Double = 0.0,
    var frozenMargin: Double = 0.0,
    var frozenCash: Double = 0.0,
    var frozenCommission: Double = 0.0,
    var cashIn: Double = 0.0,
    var commission: Double = 0.0,
    var closeProfit: Double = 0.0,
    var positionProfit: Double = 0.0,
    var preSettlementPrice: Double = 0.0,
    var settlementPrice: Double = 0.0,
    var tradingDay: String? = null,
    var settlementID: Int = 0,
    var openCost: Double = 0.0,
    var exchangeMargin: Double = 0.0,
    var combPosition: Int = 0,
    var combLongFrozen: Int = 0,
    var combShortFrozen: Int = 0,
    var closeProfitByDate: Double = 0.0,
    var closeProfitByTrade: Double = 0.0,
    var todayPosition: Int = 0,
    var marginRateByMoney: Double = 0.0,
    var marginRateByVolume: Double = 0.0,
    var strikeFrozen: Int = 0,
    var strikeFrozenAmount: Double = 0.0,
    var abandonFrozen: Int = 0,
    var exchangeID: String? = null,
    var ydStrikeFrozen: Int = 0,
    var investUnitID: String? = null,
    var positionCostOffset: Double = 0.0,
    var tasPosition: Int = 0,
    var tasPositionCost: Double = 0.0,
    var instrumentID: String? = null,
    var position: Int = 0,
) {
    companion object {
        fun from(field: CThostFtdcInvestorPositionField): InvestorPosition {
            val obj = InvestorPosition()
            obj.reserve1 = field.reserve1
            obj.brokerID = field.brokerID
            obj.investorID = field.investorID
            obj.posiDirection = field.posiDirection
            obj.hedgeFlag = field.hedgeFlag
            obj.positionDate = field.positionDate
            obj.ydPosition = field.ydPosition
            obj.longFrozen = field.longFrozen
            obj.shortFrozen = field.shortFrozen
            obj.longFrozenAmount = field.longFrozenAmount
            obj.shortFrozenAmount = field.shortFrozenAmount
            obj.openVolume = field.openVolume
            obj.closeVolume = field.closeVolume
            obj.openAmount = field.openAmount
            obj.closeAmount = field.closeAmount
            obj.positionCost = field.positionCost
            obj.preMargin = field.preMargin
            obj.useMargin = field.useMargin
            obj.frozenMargin = field.frozenMargin
            obj.frozenCash = field.frozenCash
            obj.frozenCommission = field.frozenCommission
            obj.cashIn = field.cashIn
            obj.commission = field.commission
            obj.closeProfit = field.closeProfit
            obj.positionProfit = field.positionProfit
            obj.preSettlementPrice = field.preSettlementPrice
            obj.settlementPrice = field.settlementPrice
            obj.tradingDay = field.tradingDay
            obj.settlementID = field.settlementID
            obj.openCost = field.openCost
            obj.exchangeMargin = field.exchangeMargin
            obj.combPosition = field.combPosition
            obj.combLongFrozen = field.combLongFrozen
            obj.combShortFrozen = field.combShortFrozen
            obj.closeProfitByDate = field.closeProfitByDate
            obj.closeProfitByTrade = field.closeProfitByTrade
            obj.todayPosition = field.todayPosition
            obj.marginRateByMoney = field.marginRateByMoney
            obj.marginRateByVolume = field.marginRateByVolume
            obj.strikeFrozen = field.strikeFrozen
            obj.strikeFrozenAmount = field.strikeFrozenAmount
            obj.abandonFrozen = field.abandonFrozen
            obj.exchangeID = field.exchangeID
            obj.ydStrikeFrozen = field.ydStrikeFrozen
            obj.investUnitID = field.investUnitID
            obj.positionCostOffset = field.positionCostOffset
            obj.tasPosition = field.tasPosition
            obj.tasPositionCost = field.tasPositionCost
            obj.instrumentID = field.instrumentID
            obj.position = field.position
            return obj
        }
    }
}

