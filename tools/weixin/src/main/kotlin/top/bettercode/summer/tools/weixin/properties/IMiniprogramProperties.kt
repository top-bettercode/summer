package top.bettercode.summer.tools.weixin.properties

/**
 * @author Peter Wu
 */
interface IMiniprogramProperties : IWeixinProperties {
    /**
     *  跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    val miniprogramState: String
}
