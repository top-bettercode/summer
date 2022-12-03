package top.bettercode.summer.tools.weixin.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.tomcat.util.codec.binary.Base64
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.weixin.support.WeixinResponse
import java.security.AlgorithmParameters
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class JsSession(
    @field:JsonProperty("openid")
    val openid: String? = null,

    @field:JsonProperty("session_key")
    val sessionKey: String? = null,

    @field:JsonProperty("unionid")
    val unionid: String? = null
) : WeixinResponse() {

    var userInfo: UserInfo? = null

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
        //被加密的数据
        val dataByte = Base64.decodeBase64(data)
        //加密秘钥
        val keyByte = Base64.decodeBase64(sessionKey)
        //偏移量
        val ivByte = Base64.decodeBase64(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val spec = SecretKeySpec(keyByte, "AES")
        val parameters = AlgorithmParameters.getInstance("AES")
        parameters.init(IvParameterSpec(ivByte))
        cipher.init(Cipher.DECRYPT_MODE, spec, parameters) // 初始化
        val resultByte = cipher.doFinal(dataByte)
        this.userInfo = if (null != resultByte && resultByte.isNotEmpty()) {
            StringUtil.readJson(resultByte, UserInfo::class.java)
        } else null
        return this
    }

}