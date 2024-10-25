package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.ClassUtils
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.weixin.support.WeixinResponse
import java.security.AlgorithmParameters
import java.security.Security
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class JsSession(
        @field:JsonProperty("openid")
        var openid: String? = null,

        @field:JsonProperty("session_key")
        var sessionKey: String? = null,

        @field:JsonProperty("unionid")
        var unionid: String? = null
) : WeixinResponse() {
    private val log: Logger = LoggerFactory.getLogger(JsSession::class.java)

    var userInfo: UserInfo? = null

    var phoneInfo: PhoneInfo? = null

    /**
     * AES解密
     *
     * @param data           密文，被加密的数据
     * @param iv             偏移量
     * @return
     */
    fun decrypt(data: String?, iv: String?): JsSession {
        if (data.isNullOrBlank() || iv.isNullOrBlank() || sessionKey.isNullOrBlank()) {
            return this
        }
        //如果 org.bouncycastle.jce.provider.BouncyCastleProvider 类不存在,返回
        if (!ClassUtils.isPresent("org.bouncycastle.jce.provider.BouncyCastleProvider", null)) {
            log.error("org.bouncycastle.jce.provider.BouncyCastleProvider 类不存在")
            return this
        }
        Security.addProvider(Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").getConstructor().newInstance() as BouncyCastleProvider)

        //被加密的数据
        val dataByte = Base64.getDecoder().decode(data)
        //加密秘钥
        val keyByte = Base64.getDecoder().decode(sessionKey)
        //偏移量
        val ivByte = Base64.getDecoder().decode(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val spec = SecretKeySpec(keyByte, "AES")
        val parameters = AlgorithmParameters.getInstance("AES")
        parameters.init(IvParameterSpec(ivByte))
        cipher.init(Cipher.DECRYPT_MODE, spec, parameters) // 初始化
        val resultByte = cipher.doFinal(dataByte)
        if (null != resultByte && resultByte.isNotEmpty()) {
            val info = String(resultByte)
            log.info("解密后的数据：$info")
            if (info.contains("phoneNumber")) {
                this.phoneInfo = StringUtil.readJson(resultByte, PhoneInfo::class.java)
            } else {
                this.userInfo = StringUtil.readJson(resultByte, UserInfo::class.java)
            }
        }
        return this
    }

}