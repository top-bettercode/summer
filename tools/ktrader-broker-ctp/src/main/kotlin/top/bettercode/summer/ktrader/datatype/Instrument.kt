package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcInstrumentField

/**
 * @author Peter Wu
 */
data class Instrument(
    var exchangeInstID: String? = null,
    var reserve1: String? = null,
    var exchangeID: String? = null,
    var instrumentName: String? = null,
    var reserve2: String? = null,
    var reserve3: String? = null,
    var productClass: Char = 0.toChar(),
    var deliveryYear: Int = 0,
    var deliveryMonth: Int = 0,
    var maxMarketOrderVolume: Int = 0,
    var minMarketOrderVolume: Int = 0,
    var maxLimitOrderVolume: Int = 0,
    var minLimitOrderVolume: Int = 0,
    var volumeMultiple: Int = 0,
    var priceTick: Double = 0.0,
    var createDate: String? = null,
    var openDate: String? = null,
    var expireDate: String? = null,
    var startDelivDate: String? = null,
    var endDelivDate: String? = null,
    var instLifePhase: Char = 0.toChar(),
    var isTrading: Int = 0,
    var positionType: Char = 0.toChar(),
    var positionDateType: Char = 0.toChar(),
    var longMarginRatio: Double = 0.0,
    var shortMarginRatio: Double = 0.0,
    var maxMarginSideAlgorithm: Char = 0.toChar(),
    var reserve4: String? = null,
    var strikePrice: Double = 0.0,
    var optionsType: Char = 0.toChar(),
    var underlyingMultiple: Double = 0.0,
    var combinationType: Char = 0.toChar(),
    var instrumentID: String? = null,
    var productID: String? = null,
    var underlyingInstrID: String? = null,
) {
    companion object {
        fun from(field: CThostFtdcInstrumentField): Instrument {
            val obj = Instrument()
            obj.exchangeInstID = field.exchangeInstID
            obj.reserve1 = field.reserve1
            obj.exchangeID = field.exchangeID
            obj.instrumentName = field.instrumentName
            obj.reserve2 = field.reserve2
            obj.reserve3 = field.reserve3
            obj.productClass = field.productClass
            obj.deliveryYear = field.deliveryYear
            obj.deliveryMonth = field.deliveryMonth
            obj.maxMarketOrderVolume = field.maxMarketOrderVolume
            obj.minMarketOrderVolume = field.minMarketOrderVolume
            obj.maxLimitOrderVolume = field.maxLimitOrderVolume
            obj.minLimitOrderVolume = field.minLimitOrderVolume
            obj.volumeMultiple = field.volumeMultiple
            obj.priceTick = field.priceTick
            obj.createDate = field.createDate
            obj.openDate = field.openDate
            obj.expireDate = field.expireDate
            obj.startDelivDate = field.startDelivDate
            obj.endDelivDate = field.endDelivDate
            obj.instLifePhase = field.instLifePhase
            obj.isTrading = field.isTrading
            obj.positionType = field.positionType
            obj.positionDateType = field.positionDateType
            obj.longMarginRatio = field.longMarginRatio
            obj.shortMarginRatio = field.shortMarginRatio
            obj.maxMarginSideAlgorithm = field.maxMarginSideAlgorithm
            obj.reserve4 = field.reserve4
            obj.strikePrice = field.strikePrice
            obj.optionsType = field.optionsType
            obj.underlyingMultiple = field.underlyingMultiple
            obj.combinationType = field.combinationType
            obj.instrumentID = field.instrumentID
            obj.productID = field.productID
            obj.underlyingInstrID = field.underlyingInstrID
            return obj
        }
    }
}

