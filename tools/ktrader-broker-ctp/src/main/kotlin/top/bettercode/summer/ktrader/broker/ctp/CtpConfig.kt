package top.bettercode.summer.ktrader.broker.ctp

/**
 * CtpBrokerApi 的实例化参数
 *
 * @param mdFronts 行情前置
 * @param tdFronts 交易前置
 * @param investorId 投资者资金账号
 * @param password 投资者资金账号的密码
 * @param brokerId 经纪商ID
 * @param appId 交易终端软件的标识码
 * @param authCode 交易终端软件的授权码
 * @param cachePath 存贮订阅信息文件等临时文件的目录
 * @param userProductInfo 交易终端软件的产品信息
 */
data class CtpConfig(
    val name: String,
    val mdFronts: List<String>,
    val tdFronts: List<String>,
    val investorId: String,
    val password: String,
    val brokerId: String,
    val appId: String,
    val authCode: String,
    val cachePath: String,
    val userProductInfo: String = ""
)