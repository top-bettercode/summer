package top.bettercode.summer.ktrader.broker.ctp

import org.rationalityfrontline.jctp.jctpConstants
import top.bettercode.summer.ktrader.datatype.Direction
import top.bettercode.summer.ktrader.datatype.OrderOffset

/**
 * 翻译器，用于将本地的 CTP 信息翻译为标准的 BrokerApi 信息
 */
@Suppress("MemberVisibilityCanBePrivate")
internal object Converter {

    private val THOST_FTDC_OF_Open_S = jctpConstants.THOST_FTDC_OF_Open.toString()
    private val THOST_FTDC_OF_Close_S = jctpConstants.THOST_FTDC_OF_Close.toString()
    private val THOST_FTDC_OF_CloseToday_S = jctpConstants.THOST_FTDC_OF_CloseToday.toString()
    private val THOST_FTDC_OF_CloseYesterday_S =
        jctpConstants.THOST_FTDC_OF_CloseYesterday.toString()
    val THOST_FTDC_HF_Speculation = jctpConstants.THOST_FTDC_HF_Speculation.toString()


    fun directionA2C(direction: Direction): Char {
        return when (direction) {
            Direction.LONG -> jctpConstants.THOST_FTDC_D_Buy
            Direction.SHORT -> jctpConstants.THOST_FTDC_D_Sell
            Direction.UNKNOWN -> throw IllegalArgumentException("不允许输入 UNKNOWN")
        }
    }

    fun directionC2A(direction: Char): Direction {
        return when (direction) {
            jctpConstants.THOST_FTDC_D_Buy -> Direction.LONG
            jctpConstants.THOST_FTDC_D_Sell -> Direction.SHORT
            jctpConstants.THOST_FTDC_PD_Long -> Direction.LONG
            jctpConstants.THOST_FTDC_PD_Short -> Direction.SHORT
            else -> Direction.UNKNOWN
        }
    }

    fun offsetA2C(offset: OrderOffset): String {
        return when (offset) {
            OrderOffset.OPEN -> THOST_FTDC_OF_Open_S
            OrderOffset.CLOSE -> THOST_FTDC_OF_Close_S
            OrderOffset.CLOSE_TODAY -> THOST_FTDC_OF_CloseToday_S
            OrderOffset.CLOSE_YESTERDAY -> THOST_FTDC_OF_CloseYesterday_S
            OrderOffset.UNKNOWN -> throw IllegalArgumentException("不允许输入 UNKNOWN")
        }
    }

    fun offsetC2A(offset: String): OrderOffset {
        return when (offset) {
            THOST_FTDC_OF_Open_S -> OrderOffset.OPEN
            THOST_FTDC_OF_Close_S -> OrderOffset.CLOSE
            THOST_FTDC_OF_CloseToday_S -> OrderOffset.CLOSE_TODAY
            THOST_FTDC_OF_CloseYesterday_S -> OrderOffset.CLOSE_YESTERDAY
            else -> OrderOffset.UNKNOWN
        }
    }

}