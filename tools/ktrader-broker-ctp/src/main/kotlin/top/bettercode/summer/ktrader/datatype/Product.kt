package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcProductField

class Product {
    var reserve1: String? = null

    var productName: String? = null

    var exchangeID: String? = null

    var productClass: Char = 0.toChar()

    var volumeMultiple: Int = 0

    var priceTick: Double = 0.0

    var maxMarketOrderVolume: Int = 0

    var minMarketOrderVolume: Int = 0

    var maxLimitOrderVolume: Int = 0

    var minLimitOrderVolume: Int = 0

    var positionType: Char = 0.toChar()

    var positionDateType: Char = 0.toChar()

    var closeDealType: Char = 0.toChar()

    var tradeCurrencyID: String? = null

    var mortgageFundUseRange: Char = 0.toChar()

    var reserve2: String? = null

    var underlyingMultiple: Double = 0.0

    var productID: String? = null

    var exchangeProductID: String? = null

    companion object {
        fun from(field: CThostFtdcProductField): Product {
            val obj = Product()
            obj.reserve1 = field.reserve1
            obj.productName = field.productName
            obj.exchangeID = field.exchangeID
            obj.productClass = field.productClass
            obj.volumeMultiple = field.volumeMultiple
            obj.priceTick = field.priceTick
            obj.maxMarketOrderVolume = field.maxMarketOrderVolume
            obj.minMarketOrderVolume = field.minMarketOrderVolume
            obj.maxLimitOrderVolume = field.maxLimitOrderVolume
            obj.minLimitOrderVolume = field.minLimitOrderVolume
            obj.positionType = field.positionType
            obj.positionDateType = field.positionDateType
            obj.closeDealType = field.closeDealType
            obj.tradeCurrencyID = field.tradeCurrencyID
            obj.mortgageFundUseRange = field.mortgageFundUseRange
            obj.reserve2 = field.reserve2
            obj.underlyingMultiple = field.underlyingMultiple
            obj.productID = field.productID
            obj.exchangeProductID = field.exchangeProductID
            return obj
        }
    }
}
