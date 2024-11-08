package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcDepthMarketDataField

/**
 * @author Peter Wu
 */
data class DepthMarketData(
    var bidPrice2: Double = 0.0,
    var tradingDay: String? = null,
    var reserve1: String? = null,
    var exchangeID: String? = null,
    var reserve2: String? = null,
    var lastPrice: Double = 0.0,
    var preSettlementPrice: Double = 0.0,
    var preClosePrice: Double = 0.0,
    var preOpenInterest: Double = 0.0,
    var openPrice: Double = 0.0,
    var highestPrice: Double = 0.0,
    var lowestPrice: Double = 0.0,
    var volume: Int = 0,
    var turnover: Double = 0.0,
    var openInterest: Double = 0.0,
    var closePrice: Double = 0.0,
    var settlementPrice: Double = 0.0,
    var upperLimitPrice: Double = 0.0,
    var lowerLimitPrice: Double = 0.0,
    var preDelta: Double = 0.0,
    var currDelta: Double = 0.0,
    var updateTime: String? = null,
    var updateMillisec: Int = 0,
    var bidPrice1: Double = 0.0,
    var bidVolume1: Int = 0,
    var askPrice1: Double = 0.0,
    var askVolume1: Int = 0,
    var bidVolume2: Int = 0,
    var askPrice2: Double = 0.0,
    var askVolume2: Int = 0,
    var bidPrice3: Double = 0.0,
    var bidVolume3: Int = 0,
    var askPrice3: Double = 0.0,
    var askVolume3: Int = 0,
    var bidPrice4: Double = 0.0,
    var bidVolume4: Int = 0,
    var askPrice4: Double = 0.0,
    var askVolume4: Int = 0,
    var bidPrice5: Double = 0.0,
    var bidVolume5: Int = 0,
    var askPrice5: Double = 0.0,
    var askVolume5: Int = 0,
    var averagePrice: Double = 0.0,
    var actionDay: String? = null,
    var instrumentID: String? = null,
    var exchangeInstID: String? = null,
    var bandingUpperPrice: Double = 0.0,
    var bandingLowerPrice: Double = 0.0,
) {
    companion object {
        fun from(field: CThostFtdcDepthMarketDataField): DepthMarketData {
            val obj = DepthMarketData()
            obj.bidPrice2 = field.bidPrice2
            obj.tradingDay = field.tradingDay
            obj.reserve1 = field.reserve1
            obj.exchangeID = field.exchangeID
            obj.reserve2 = field.reserve2
            obj.lastPrice = field.lastPrice
            obj.preSettlementPrice = field.preSettlementPrice
            obj.preClosePrice = field.preClosePrice
            obj.preOpenInterest = field.preOpenInterest
            obj.openPrice = field.openPrice
            obj.highestPrice = field.highestPrice
            obj.lowestPrice = field.lowestPrice
            obj.volume = field.volume
            obj.turnover = field.turnover
            obj.openInterest = field.openInterest
            obj.closePrice = field.closePrice
            obj.settlementPrice = field.settlementPrice
            obj.upperLimitPrice = field.upperLimitPrice
            obj.lowerLimitPrice = field.lowerLimitPrice
            obj.preDelta = field.preDelta
            obj.currDelta = field.currDelta
            obj.updateTime = field.updateTime
            obj.updateMillisec = field.updateMillisec
            obj.bidPrice1 = field.bidPrice1
            obj.bidVolume1 = field.bidVolume1
            obj.askPrice1 = field.askPrice1
            obj.askVolume1 = field.askVolume1
            obj.bidVolume2 = field.bidVolume2
            obj.askPrice2 = field.askPrice2
            obj.askVolume2 = field.askVolume2
            obj.bidPrice3 = field.bidPrice3
            obj.bidVolume3 = field.bidVolume3
            obj.askPrice3 = field.askPrice3
            obj.askVolume3 = field.askVolume3
            obj.bidPrice4 = field.bidPrice4
            obj.bidVolume4 = field.bidVolume4
            obj.askPrice4 = field.askPrice4
            obj.askVolume4 = field.askVolume4
            obj.bidPrice5 = field.bidPrice5
            obj.bidVolume5 = field.bidVolume5
            obj.askPrice5 = field.askPrice5
            obj.askVolume5 = field.askVolume5
            obj.averagePrice = field.averagePrice
            obj.actionDay = field.actionDay
            obj.instrumentID = field.instrumentID
            obj.exchangeInstID = field.exchangeInstID
            obj.bandingUpperPrice = field.bandingUpperPrice
            obj.bandingLowerPrice = field.bandingLowerPrice
            return obj
        }
    }
}

