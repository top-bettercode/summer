package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcInstrumentCommissionRateField

class InstrumentCommissionRate {
    var reserve1: String? = null

    var investorRange: Char = 0.toChar()

    var brokerID: String? = null

    var investorID: String? = null

    var openRatioByMoney: Double = 0.0

    var openRatioByVolume: Double = 0.0

    var closeRatioByMoney: Double = 0.0

    var closeRatioByVolume: Double = 0.0

    var closeTodayRatioByMoney: Double = 0.0

    var closeTodayRatioByVolume: Double = 0.0

    var exchangeID: String? = null

    var bizType: Char = 0.toChar()

    var investUnitID: String? = null

    var instrumentID: String? = null

    companion object {
        fun from(field: CThostFtdcInstrumentCommissionRateField): InstrumentCommissionRate {
            val obj = InstrumentCommissionRate()
            obj.reserve1 = field.reserve1
            obj.investorRange = field.investorRange
            obj.brokerID = field.brokerID
            obj.investorID = field.investorID
            obj.openRatioByMoney = field.openRatioByMoney
            obj.openRatioByVolume = field.openRatioByVolume
            obj.closeRatioByMoney = field.closeRatioByMoney
            obj.closeRatioByVolume = field.closeRatioByVolume
            obj.closeTodayRatioByMoney = field.closeTodayRatioByMoney
            obj.closeTodayRatioByVolume = field.closeTodayRatioByVolume
            obj.exchangeID = field.exchangeID
            obj.bizType = field.bizType
            obj.investUnitID = field.investUnitID
            obj.instrumentID = field.instrumentID
            return obj
        }
    }
}
