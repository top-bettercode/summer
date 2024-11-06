@file:Suppress("unused")

package org.rationalityfrontline.ktrader.datatype

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Tick
 * @param code 证券代码
 * @param time 产生时间
 * @param lastPrice 最新价
 * @param bidPrice 挂买价（买一，买二，买三 ...）
 * @param askPrice 挂卖价（卖一，卖二，卖三 ...）
 * @param bidVolume 挂买量（买一，买二，买三 ...）
 * @param askVolume 挂卖量（卖一，卖二，卖三 ...）
 * @param volume Tick 内的成交量
 * @param turnover Tick 内的成交额
 * @param openInterestDelta Tick 内的持仓量变动
 * @param direction Tick 的方向
 * @param status 证券当前的市场状态
 * @param preClosePrice 昨日收盘价
 * @param preSettlementPrice 昨日结算价
 * @param preOpenInterest 昨日持仓量
 * @param todayOpenPrice 今日开盘价
 * @param todayClosePrice 今日收盘价
 * @param todayHighPrice 今日最高价
 * @param todayLowPrice 今日最低价
 * @param todayHighLimitPrice 今日涨停价
 * @param todayLowLimitPrice 今日跌停价
 * @param todaySettlementPrice 今日结算价
 * @param todayAvgPrice 今日成交均价
 * @param todayVolume 今日成交量
 * @param todayTurnover 今日成交额
 * @param todayOpenInterest 今日持仓量
 * @param extras 额外数据
 */
data class Tick(
    val code: String,
    val time: LocalDateTime,
    val lastPrice: Double,
    val bidPrice: Array<Double>,
    val askPrice: Array<Double>,
    val bidVolume: Array<Int>,
    val askVolume: Array<Int>,
    val volume: Int,
    val turnover: Double,
    val openInterestDelta: Int,
    val direction: TickDirection,
    var status: MarketStatus,
    val preClosePrice: Double,
    val preSettlementPrice: Double,
    val preOpenInterest: Int,
    val todayOpenPrice: Double,
    val todayClosePrice: Double,
    val todayLowPrice: Double,
    val todayHighPrice: Double,
    val todayLowLimitPrice: Double,
    val todayHighLimitPrice: Double,
    val todaySettlementPrice: Double,
    val todayAvgPrice: Double,
    val todayVolume: Int,
    val todayTurnover: Double,
    val todayOpenInterest: Int,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Tick {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * Bar
 * @param code 证券代码
 * @param interval Bar 的时长，以秒为单位。如 300 代表该 Bar 时长为 5 分钟，3600 代表 1 小时， 86400 代表 1 天
 * @param startTime Bar 开始的时间
 * @param endTime Bar 结束的时间
 * @param openPrice 开盘价
 * @param closePrice 收盘价
 * @param lowPrice 最低价
 * @param volume 成交量
 * @param turnover 成交额
 * @param openInterestDelta 持仓量变动
 * @param extras 额外数据
 */
data class Bar(
    val code: String,
    val interval: Int,
    val startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var openPrice: Double,
    var closePrice: Double,
    var lowPrice: Double,
    var highPrice: Double,
    var volume: Int,
    var turnover: Double,
    var openInterestDelta: Int,
    var extras: MutableMap<String, String>? = null,
) {
    companion object {
        /**
         * 创建新的 Bar
         */
        fun createBar(code: String, interval: Int, startTime: LocalDateTime, endTime: LocalDateTime = startTime.plusSeconds(interval.toLong()), openPrice: Double = 0.0): Bar {
            return Bar(
                code = code,
                interval = interval,
                startTime = startTime,
                endTime = endTime,
                openPrice = openPrice,
                closePrice = openPrice,
                lowPrice = if (openPrice == 0.0) Double.MAX_VALUE else openPrice,
                highPrice = if (openPrice == 0.0) Double.MIN_VALUE else openPrice,
                volume = 0,
                turnover = 0.0,
                openInterestDelta = 0,
            )
        }
    }

    /**
     * 用 [tick] 更新当前 Bar 的数值（不会更新 [endTime] 与 [openPrice]）
     */
    fun updateTick(tick: Tick) {
        if (openPrice == 0.0) openPrice = tick.lastPrice
        lowPrice = min(lowPrice, tick.lastPrice)
        highPrice = max(highPrice, tick.lastPrice)
        closePrice = tick.lastPrice
        volume += tick.volume
        turnover += tick.turnover
        openInterestDelta += tick.openInterestDelta
    }

    /**
     * 用 [bar] 更新当前 Bar 的数值（不会更新 [endTime] 与 [openPrice]）
     */
    fun updateBar(bar: Bar) {
        if (openPrice == 0.0) openPrice = bar.openPrice
        lowPrice = min(lowPrice, bar.lowPrice)
        highPrice = max(highPrice, bar.highPrice)
        closePrice = bar.closePrice
        volume += bar.volume
        turnover += bar.turnover
        openInterestDelta += bar.openInterestDelta
    }

    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Bar {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * Bar 的信息
 * @param code 证券代码
 * @param interval Bar 的时长，以秒为单位。如 300 代表该 Bar 时长为 5 分钟，3600 代表 1 小时， 86400 代表 1 天
 */
data class BarInfo(
    val code: String,
    val interval: Int,
)

/**
 * Order
 * @param accountId 资金账号
 * @param orderId 订单 ID
 * @param code 证券代码
 * @param price 订单价格
 * @param closePositionPrice 指定的所平仓位的成本价
 * @param volume 订单数量
 * @param minVolume 最小成交量，仅当订单类型为 FAK 时生效
 * @param direction 订单交易方向
 * @param offset 仓位开平类型
 * @param orderType 订单类型
 * @param status 订单状态
 * @param statusMsg 订单状态描述
 * @param filledVolume 已成交数量
 * @param turnover 已成交金额（期货/期权是成交价*成交量*合约乘数）
 * @param avgFillPrice 平均成交价格
 * @param frozenCash 挂单冻结资金（仅限开仓）
 * @param commission 手续费
 * @param createTime 订单产生时间
 * @param updateTime 订单更新时间
 * @param extras 额外数据
 */
data class Order(
    var accountId: String,
    val orderId: String,
    val code: String,
    val price: Double,
    var closePositionPrice: Double?,
    val volume: Int,
    val minVolume: Int,
    val direction: Direction,
    val offset: OrderOffset,
    val orderType: OrderType,
    var status: OrderStatus,
    var statusMsg: String,
    var filledVolume: Int,
    var turnover: Double,
    var avgFillPrice: Double,
    var frozenCash: Double,
    var commission: Double,
    val createTime: LocalDateTime,
    var updateTime: LocalDateTime,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 用 [order] 更新自身字段
     */
    fun update(order: Order) {
        status = order.status
        statusMsg = order.statusMsg
        filledVolume = order.filledVolume
        turnover = order.turnover
        avgFillPrice = order.avgFillPrice
        frozenCash = order.frozenCash
        commission = order.commission
        updateTime = order.updateTime
        if (extras == null) {
            extras = order.extras
        } else {
            if (order.extras != null) {
                extras?.putAll(order.extras!!)
            }
        }
    }

    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Order {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * Trade
 * @param accountId 资金账号
 * @param tradeId 成交记录 ID
 * @param orderId 对应的订单 ID
 * @param code 证券代码
 * @param price 成交价
 * @param volume 成交量
 * @param turnover 成交额（期货/期权是成交价*成交量*合约乘数）
 * @param direction 交易方向
 * @param offset 开平仓类型
 * @param commission 手续费
 * @param time 成交时间
 * @param extras 额外数据
 */
data class Trade(
    var accountId: String,
    val tradeId: String,
    val orderId: String,
    val code: String,
    val price: Double,
    val volume: Int,
    var turnover: Double,
    val direction: Direction,
    var offset: OrderOffset,
    var commission: Double,
    val time: LocalDateTime,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Trade {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * 证券信息
 * @param code 证券代码
 * @param type 证券类型
 * @param productId 证券的产品 ID （仅期货/期权）
 * @param name 证券名称
 * @param priceTick 最小变动价格
 * @param isTrading 是否处于可交易状态
 * @param openDate 上市日
 * @param expireDate 最后交易日
 * @param endDeliveryDate 最后交割日
 * @param volumeMultiple 合约乘数
 * @param isUseMaxMarginSideAlgorithm 是否使用大额单边保证金算法
 * @param marginRate 保证金率
 * @param commissionRate 手续费率
 * @param optionsType 期权类型
 * @param optionsUnderlyingCode 期权对应的基础证券代码
 * @param optionsStrikePrice 期权行权价格
 * @param extras 额外数据
 */
data class SecurityInfo(
    val code: String,
    val type: SecurityType,
    val productId: String = "",
    val name: String,
    val priceTick: Double,
    val isTrading: Boolean,
    val openDate: LocalDate?,
    val expireDate: LocalDate? = null,
    val endDeliveryDate: LocalDate? = null,
    val volumeMultiple: Int = 1,
    val isUseMaxMarginSideAlgorithm: Boolean = false,
    var marginRate: MarginRate? = null,
    var commissionRate: CommissionRate? = null,
    val optionsType: OptionsType = OptionsType.UNKNOWN,
    val optionsUnderlyingCode: String = "",
    val optionsStrikePrice: Double = 0.0,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): SecurityInfo {
        return copy(
            marginRate = marginRate?.deepCopy(),
            commissionRate = commissionRate?.deepCopy(),
            extras = extras?.toMutableMap()
        )
    }
}

/**
 * 资产
 *
 * [total] = [available] + [positionValue] + [frozenByOrder]
 *
 * [total] = [initialCash] + [totalClosePnl] + [positionPnl] - [totalCommission]
 *
 * @param accountId 资金账号
 * @param total 全部资产（折合现金）
 * @param available 可用资金
 * @param positionValue 持仓占用资金
 * @param frozenByOrder 挂单占用资金
 * @param todayCommission 今日手续费
 * @param initialCash 初始资金（即累计入金 - 累计出金）
 * @param totalClosePnl 累计平仓盈亏
 * @param positionPnl 当前持仓盈亏
 * @param totalCommission 累计手续费
 * @param extras 额外数据
 */
data class Assets(
    var accountId: String,
    var tradingDay: LocalDate,
    var total: Double,
    var available: Double,
    var positionValue: Double,
    var positionPnl: Double,
    var frozenByOrder: Double,
    var todayCommission: Double,
    var initialCash: Double,
    var totalClosePnl: Double,
    var totalCommission: Double,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 计算并更新 [total]（要求 [initialCash], [totalClosePnl], [positionPnl], [totalCommission] 已到位）
     */
    fun calculateTotal(): Double {
        total = initialCash + totalClosePnl + positionPnl - totalCommission
        return total
    }

    /**
     * 计算并更新 [available]（要求 [total], [positionValue], [frozenByOrder] 已到位）
     */
    fun calculateAvailable(): Double {
        available = total - positionValue - frozenByOrder
        return available
    }

    /**
     * 更新数值（先调用 [calculateTotal], 然后调用 [calculateAvailable]）
     */
    fun update() {
        calculateTotal()
        calculateAvailable()
    }

    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Assets {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * 持仓汇总信息
 * @param accountId 资金账号
 * @param code 证券代码
 * @param direction 持仓方向
 * @param preVolume 昨日持仓量
 * @param volume 持仓量
 * @param value 持仓占用资金
 * @param todayVolume 今仓数量
 * @param frozenVolume 冻结持仓量
 * @param frozenTodayVolume 冻结的今仓量
 * @param todayOpenVolume 今日累计开仓量
 * @param todayCloseVolume 今日累计平仓量
 * @param todayCommission 今日手续费
 * @param openCost 开仓成本（期货/期权是Σ成交价*成交量*合约乘数）
 * @param avgOpenPrice 开仓均价
 * @param lastPrice 最新价
 * @param pnl 净盈亏额
 * @param extras 额外数据
 * @property yesterdayVolume 昨仓数量
 * @property frozenYesterdayVolume 冻结的昨仓量
 */
data class Position(
    var accountId: String,
    var tradingDay: LocalDate,
    val code: String,
    val direction: Direction,
    var preVolume: Int,
    var volume: Int,
    var todayVolume: Int,
    var frozenVolume: Int,
    var frozenTodayVolume: Int,
    var todayOpenVolume: Int,
    var todayCloseVolume: Int,
    var todayCommission: Double,
    var openCost: Double,
    var avgOpenPrice: Double,
    var lastPrice: Double = 0.0,
    var value: Double = 0.0,
    var pnl: Double = 0.0,
    var extras: MutableMap<String, String>? = null,
) {
    val yesterdayVolume: Int get() = volume - todayVolume
    val frozenYesterdayVolume: Int get() = frozenVolume - frozenTodayVolume

    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): Position {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * 持仓明细
 * @param accountId 资金账号
 * @param code 证券代码
 * @param direction 持仓方向
 * @param details 持仓明细记录
 */
data class PositionDetails(
    var accountId: String,
    val code: String,
    val direction: Direction,
    val details: MutableList<PositionDetail> = mutableListOf(),
) {

    /**
     * 生成对应的持仓汇总信息
     */
    fun toPosition(tradingDay: LocalDate, volumeMultiple: Int, preVolume: Int, todayOpenVolume: Int, todayCloseVolume: Int, todayCommission: Double, extras: MutableMap<String, String>? = null): Position {
        var volume = 0
        var todayVolume = 0
        var openCost = 0.0
        details.forEach {
            volume += it.volume
            todayVolume += it.todayVolume
            openCost += it.price * it.volume * volumeMultiple
        }
        return Position(
            accountId = accountId,
            tradingDay = tradingDay,
            code = code,
            direction = direction,
            preVolume = preVolume,
            volume = volume,
            value = 0.0,
            todayVolume = todayVolume,
            frozenVolume = 0,
            frozenTodayVolume = 0,
            todayOpenVolume = todayOpenVolume,
            todayCloseVolume = todayCloseVolume,
            todayCommission = todayCommission,
            openCost = openCost,
            avgOpenPrice = openCost / volumeMultiple,
            extras = extras,
        )
    }

    /**
     * 更新已存的持仓汇总对象
     */
    fun updatePosition(position: Position, volumeMultiple: Int) {
        var volume = 0
        var todayVolume = 0
        var openCost = 0.0
        details.forEach {
            volume += it.volume
            todayVolume += it.todayVolume
            openCost += it.price * it.volume * volumeMultiple
        }
        position.volume = volume
        position.todayVolume = todayVolume
        position.openCost = openCost
        position.avgOpenPrice = openCost / volume / volumeMultiple
    }

    /**
     * 返回开仓价为 [price] 的持仓明细。如果不存在该开仓价的持仓记录，则返回 null
     */
    operator fun get(price: Double): PositionDetail? {
        val index = details.binarySearch { sign(it.price - price).toInt()}
        return if (index < 0) null else details[index]
    }

    /**
     * 返回开仓价为 [price] 的持仓明细。如果不存在该开仓价的持仓记录，则插入一条空白的持仓明细并返回该新建的持仓明细
     */
    fun getOrPut(price: Double): PositionDetail {
        return get(price) ?: run {
            val detail = PositionDetail(accountId = accountId, code = code, direction = direction, price = price)
            var i = details.indexOfFirst { it.price > price }
            i = if (i == -1) details.size else i
            details.add(i, detail)
            return@run detail
        }
    }

    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): PositionDetails {
        return copy(details = details.map { it.copy() }.toMutableList())
    }
}

/**
 * 单条持仓明细记录
 * @param accountId 资金账号
 * @param code 证券代码
 * @param direction 持仓方向
 * @param price 开仓价格
 * @param volume 持仓数量
 * @param todayVolume 今仓数量
 * @param updateTime 最后持仓变动时间
 * @property yesterdayVolume 昨仓数量
 */
data class PositionDetail(
    var accountId: String,
    val code: String,
    val direction: Direction,
    val price: Double,
    var volume: Int = 0,
    var todayVolume: Int = 0,
    var updateTime: LocalDateTime = LocalDateTime.MIN,
) {
    val yesterdayVolume: Int get() = volume - todayVolume
}

/**
 * 记录单一合约的双向持仓
 */
data class BiPosition(
    var long: Position? = null,
    var short: Position? = null,
)

/**
 * 记录单一合约的双向持仓明细
 */
data class BiPositionDetails(
    var long: PositionDetails? = null,
    var short: PositionDetails? = null,
)

/**
 * 从 [Position] 集合生成 [BiPosition] Map (key 为 code, value 为 BiPosition)
 */
fun Collection<Position>.toBiPositionsMap(): MutableMap<String, BiPosition> {
    val biMap = mutableMapOf<String, BiPosition>()
    forEach {
        val biPosition = biMap.getOrPut(it.code) { BiPosition() }
        when (it.direction) {
            Direction.LONG -> biPosition.long = it
            Direction.SHORT -> biPosition.short = it
            else -> Unit
        }
    }
    return biMap
}

/**
 * 从 [PositionDetail] 集合生成 [BiPositionDetails] Map (key 为 code, value 为 BiPositionDetails)
 * @param accountId 用于过滤 PositionDetail 的账户 ID
 */
fun Collection<PositionDetail>.toBiPositionDetailsMap(accountId: String? = null): MutableMap<String, BiPositionDetails> {
    val biMap = mutableMapOf<String, BiPositionDetails>()
    if (isEmpty()) return biMap
    val account = accountId ?: first().accountId
    forEach { detail ->
        if (detail.accountId != account) return@forEach
        val biPositionDetails = biMap.getOrPut(detail.code) { BiPositionDetails() }
        var details: PositionDetails? = null
        when (detail.direction) {
            Direction.LONG -> {
                if (biPositionDetails.long == null) {
                    biPositionDetails.long = PositionDetails(detail.accountId, detail.code, detail.direction)
                }
                details = biPositionDetails.long!!
            }
            Direction.SHORT -> {
                if (biPositionDetails.short == null) {
                    biPositionDetails.short = PositionDetails(detail.accountId, detail.code, detail.direction)
                }
                details = biPositionDetails.short!!
            }
            else -> Unit
        }
        if (details != null) {
            val index = details.details.binarySearch { sign(it.price - detail.price).toInt() }
            if (index >= 0) {
                details.details[index].apply {
                    volume += detail.volume
                    todayVolume += detail.todayVolume
                    if (updateTime.isBefore(detail.updateTime)) {
                        updateTime = detail.updateTime
                    }
                }
            } else {
                details.details.add(-index - 1, detail)
            }
        }
    }
    return biMap
}

/**
 * 手续费率
 * @param code 证券代码
 * @param openRatioByMoney 开仓手续费率（按成交额）
 * @param openRatioByVolume 开仓手续费（按手数）
 * @param closeRatioByMoney 平仓手续费率（按成交额）
 * @param closeRatioByVolume 平仓手续费（按手数）
 * @param closeTodayRatioByMoney 平今仓手续费率（按成交额）
 * @param closeTodayRatioByVolume 平今仓手续费（按手数）
 * @param orderInsertFeeByVolume 报单手续费（按手数）
 * @param orderInsertFeeByTrade 报单手续费（按订单）
 * @param orderCancelFeeByVolume 撤单手续费（按手数）
 * @param orderCancelFeeByTrade 撤单手续费（按订单）
 * @param optionsStrikeRatioByMoney 期权行权手续费率（按金额）
 * @param optionsStrikeRatioByVolume 期权行权手续费（按手数）
 * @param extras 额外数据
 */
data class CommissionRate(
    val code: String,
    val openRatioByMoney: Double,
    val openRatioByVolume: Double,
    val closeRatioByMoney: Double,
    val closeRatioByVolume: Double,
    val closeTodayRatioByMoney: Double,
    val closeTodayRatioByVolume: Double,
    var orderInsertFeeByTrade: Double = 0.0,
    var orderInsertFeeByVolume: Double = 0.0,
    var orderCancelFeeByTrade: Double = 0.0,
    var orderCancelFeeByVolume: Double = 0.0,
    val optionsStrikeRatioByMoney: Double = 0.0,
    val optionsStrikeRatioByVolume: Double = 0.0,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): CommissionRate {
        return copy(extras = extras?.toMutableMap())
    }
}

/**
 * 期货/期权保证金率
 * @param code 证券代码
 * @param longMarginRatioByMoney 多头保证金率（按金额）。当证券为期权时表示期权固定保证金
 * @param longMarginRatioByVolume 多头保证金（按手数）。当证券为期权时表示期权交易所固定保证金
 * @param shortMarginRatioByMoney 空头保证金率（按金额）。当证券为期权时表示期权最小保证金
 * @param shortMarginRatioByVolume 空头保证金（按手数）。当证券为期权时表示期权交易所最小保证金
 * @param extras 额外数据
 */
data class MarginRate(
    val code: String,
    val longMarginRatioByMoney: Double,
    val longMarginRatioByVolume: Double,
    val shortMarginRatioByMoney: Double,
    val shortMarginRatioByVolume: Double,
    var extras: MutableMap<String, String>? = null,
) {
    /**
     * 返回一份自身的完全拷贝
     */
    fun deepCopy(): MarginRate {
        return copy(extras = extras?.toMutableMap())
    }
}