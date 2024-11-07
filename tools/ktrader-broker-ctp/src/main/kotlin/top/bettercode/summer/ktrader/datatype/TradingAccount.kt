package top.bettercode.summer.ktrader.datatype

import org.rationalityfrontline.jctp.CThostFtdcTradingAccountField

class TradingAccount {
    var brokerID: String? = null

    var accountID: String? = null

    var preMortgage: Double = 0.0

    var preCredit: Double = 0.0

    var preDeposit: Double = 0.0

    var preBalance: Double = 0.0

    var preMargin: Double = 0.0

    var interestBase: Double = 0.0

    var interest: Double = 0.0

    var deposit: Double = 0.0

    var withdraw: Double = 0.0

    var frozenMargin: Double = 0.0

    var frozenCash: Double = 0.0

    var frozenCommission: Double = 0.0

    var currMargin: Double = 0.0

    var cashIn: Double = 0.0

    var commission: Double = 0.0

    var closeProfit: Double = 0.0

    var positionProfit: Double = 0.0

    var balance: Double = 0.0

    var available: Double = 0.0

    var withdrawQuota: Double = 0.0

    var reserve: Double = 0.0

    var tradingDay: String? = null

    var settlementID: Int = 0

    var credit: Double = 0.0

    var mortgage: Double = 0.0

    var exchangeMargin: Double = 0.0

    var deliveryMargin: Double = 0.0

    var exchangeDeliveryMargin: Double = 0.0

    var reserveBalance: Double = 0.0

    var currencyID: String? = null

    var preFundMortgageIn: Double = 0.0

    var preFundMortgageOut: Double = 0.0

    var fundMortgageIn: Double = 0.0

    var fundMortgageOut: Double = 0.0

    var fundMortgageAvailable: Double = 0.0

    var mortgageableFund: Double = 0.0

    var specProductMargin: Double = 0.0

    var specProductFrozenMargin: Double = 0.0

    var specProductCommission: Double = 0.0

    var specProductFrozenCommission: Double = 0.0

    var specProductPositionProfit: Double = 0.0

    var specProductCloseProfit: Double = 0.0

    var specProductPositionProfitByAlg: Double = 0.0

    var specProductExchangeMargin: Double = 0.0

    var bizType: Char = 0.toChar()

    var frozenSwap: Double = 0.0

    var remainSwap: Double = 0.0

    companion object {
        fun from(field: CThostFtdcTradingAccountField): TradingAccount {
            val obj = TradingAccount()
            obj.brokerID = field.brokerID
            obj.accountID = field.accountID
            obj.preMortgage = field.preMortgage
            obj.preCredit = field.preCredit
            obj.preDeposit = field.preDeposit
            obj.preBalance = field.preBalance
            obj.preMargin = field.preMargin
            obj.interestBase = field.interestBase
            obj.interest = field.interest
            obj.deposit = field.deposit
            obj.withdraw = field.withdraw
            obj.frozenMargin = field.frozenMargin
            obj.frozenCash = field.frozenCash
            obj.frozenCommission = field.frozenCommission
            obj.currMargin = field.currMargin
            obj.cashIn = field.cashIn
            obj.commission = field.commission
            obj.closeProfit = field.closeProfit
            obj.positionProfit = field.positionProfit
            obj.balance = field.balance
            obj.available = field.available
            obj.withdrawQuota = field.withdrawQuota
            obj.reserve = field.reserve
            obj.tradingDay = field.tradingDay
            obj.settlementID = field.settlementID
            obj.credit = field.credit
            obj.mortgage = field.mortgage
            obj.exchangeMargin = field.exchangeMargin
            obj.deliveryMargin = field.deliveryMargin
            obj.exchangeDeliveryMargin = field.exchangeDeliveryMargin
            obj.reserveBalance = field.reserveBalance
            obj.currencyID = field.currencyID
            obj.preFundMortgageIn = field.preFundMortgageIn
            obj.preFundMortgageOut = field.preFundMortgageOut
            obj.fundMortgageIn = field.fundMortgageIn
            obj.fundMortgageOut = field.fundMortgageOut
            obj.fundMortgageAvailable = field.fundMortgageAvailable
            obj.mortgageableFund = field.mortgageableFund
            obj.specProductMargin = field.specProductMargin
            obj.specProductFrozenMargin = field.specProductFrozenMargin
            obj.specProductCommission = field.specProductCommission
            obj.specProductFrozenCommission = field.specProductFrozenCommission
            obj.specProductPositionProfit = field.specProductPositionProfit
            obj.specProductCloseProfit = field.specProductCloseProfit
            obj.specProductPositionProfitByAlg = field.specProductPositionProfitByAlg
            obj.specProductExchangeMargin = field.specProductExchangeMargin
            obj.bizType = field.bizType
            obj.frozenSwap = field.frozenSwap
            obj.remainSwap = field.remainSwap
            return obj
        }
    }
}
