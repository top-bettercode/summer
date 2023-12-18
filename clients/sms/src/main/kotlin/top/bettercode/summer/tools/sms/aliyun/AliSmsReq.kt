package top.bettercode.summer.tools.sms.aliyun

/**
 * @author Peter Wu
 */
class AliSmsReq {
    //--------------------------------------------
    /**
     * 接收短信的手机号码
     */
    var phoneNumber: String = ""

    /**
     * 短信签名名称
     */
    var signName: String = ""

    /**
     * 短信模板变量对应的实际值
     */
    var templateParam: Map<String, String> = mapOf()

    constructor()
    constructor(phoneNumber: String, signName: String,
                templateParam: Map<String, String>) {
        this.phoneNumber = phoneNumber
        this.signName = signName
        this.templateParam = templateParam
    }

    companion object {
        //--------------------------------------------
        fun of(phoneNumber: String, signName: String,
               templateParam: Map<String, String>): AliSmsReq {
            return AliSmsReq(phoneNumber, signName, templateParam)
        }
    }
}
