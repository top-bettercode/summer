package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcTradeField

class Trade {
    var exchangeInstID: String? = null

    var userID: String? = null

    var brokerID: String? = null

    var investorID: String? = null

    var reserve1: String? = null

    var orderRef: String? = null

    var exchangeID: String? = null

    var tradeID: String? = null

    var direction: Char = 0.toChar()

    var orderSysID: String? = null

    var participantID: String? = null

    var clientID: String? = null

    var tradingRole: Char = 0.toChar()

    var reserve2: String? = null

    var offsetFlag: Char = 0.toChar()

    var hedgeFlag: Char = 0.toChar()

    var price: Double = 0.0

    var volume: Int = 0

    var tradeDate: String? = null

    var tradeTime: String? = null

    var tradeType: Char = 0.toChar()

    var priceSource: Char = 0.toChar()

    var traderID: String? = null

    var orderLocalID: String? = null

    var clearingPartID: String? = null

    var businessUnit: String? = null

    var sequenceNo: Int = 0

    var tradingDay: String? = null

    var settlementID: Int = 0

    var brokerOrderSeq: Int = 0

    var tradeSource: Char = 0.toChar()

    var investUnitID: String? = null

    var instrumentID: String? = null

    companion object {
        fun from(field: CThostFtdcTradeField): Trade {
            val obj = Trade()
            obj.exchangeInstID = field.exchangeInstID
            obj.userID = field.userID
            obj.brokerID = field.brokerID
            obj.investorID = field.investorID
            obj.reserve1 = field.reserve1
            obj.orderRef = field.orderRef
            obj.exchangeID = field.exchangeID
            obj.tradeID = field.tradeID
            obj.direction = field.direction
            obj.orderSysID = field.orderSysID
            obj.participantID = field.participantID
            obj.clientID = field.clientID
            obj.tradingRole = field.tradingRole
            obj.reserve2 = field.reserve2
            obj.offsetFlag = field.offsetFlag
            obj.hedgeFlag = field.hedgeFlag
            obj.price = field.price
            obj.volume = field.volume
            obj.tradeDate = field.tradeDate
            obj.tradeTime = field.tradeTime
            obj.tradeType = field.tradeType
            obj.priceSource = field.priceSource
            obj.traderID = field.traderID
            obj.orderLocalID = field.orderLocalID
            obj.clearingPartID = field.clearingPartID
            obj.businessUnit = field.businessUnit
            obj.sequenceNo = field.sequenceNo
            obj.tradingDay = field.tradingDay
            obj.settlementID = field.settlementID
            obj.brokerOrderSeq = field.brokerOrderSeq
            obj.tradeSource = field.tradeSource
            obj.investUnitID = field.investUnitID
            obj.instrumentID = field.instrumentID
            return obj
        }
    }
}
