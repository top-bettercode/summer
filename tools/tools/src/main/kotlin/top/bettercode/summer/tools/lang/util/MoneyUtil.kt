package top.bettercode.summer.tools.lang.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 金额相关计算工具类
 *
 * @author Peter Wu
 */
object MoneyUtil {

    /**
     * @param yuan 单位元
     * @return 单位分
     */
    @JvmStatic
    fun toCent(yuan: String): Long {
        return BigDecimal(yuan).setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100)).toLong()
    }

    /**
     * @param yuan 单位元
     * @return 单位分
     */
    @JvmStatic
    fun toCent(yuan: BigDecimal): Long {
        return yuan.setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100)).toLong()
    }


    /**
     * @param cent 单位分
     * @return 单位元
     */
    @JvmStatic
    fun toYun(cent: Long): BigDecimal {
        return toYun(cent, 2)
    }

    /**
     * @param cent 单位分
     * @return 单位元
     */
    @JvmStatic
    fun toYun(cent: Long, scale: Int): BigDecimal {
        return toYun(BigDecimal(cent), scale)
    }


    /**
     * @param cent 单位分
     * @param scale 小数位数
     * @return 单位元
     */
    @JvmStatic
    fun toYun(cent: BigDecimal, scale: Int): BigDecimal {
        return cent.divide(BigDecimal(100), scale, RoundingMode.HALF_UP)
    }
}
