package top.bettercode.summer.tools.weixin.support.aes

import org.springframework.util.Base64Utils
import top.bettercode.summer.tools.weixin.support.aes.AesException
import top.bettercode.summer.tools.weixin.support.aes.PKCS7Encoder.decode
import top.bettercode.summer.tools.weixin.support.aes.PKCS7Encoder.encode
import top.bettercode.summer.tools.weixin.support.offiaccount.IOffiaccountClient.Companion.shaHex
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 提供接收和推送给公众平台消息的加解密接口(UTF8编码的字符串).
 *
 *  1. 第三方回复加密消息给公众平台
 *  1. 第三方收到公众平台发送的消息，验证消息的安全性，并对消息进行解密。
 *
 * 说明：异常java.security.InvalidKeyException:illegal Key Size的解决方案
 *
 *  1. 在官方网站下载JCE无限制权限策略文件（JDK7的下载地址：
 * http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
 *  1. 下载后解压，可以看到local_policy.jar和US_export_policy.jar以及readme.txt
 *  1. 如果安装了JRE，将两个jar文件放到%JRE_HOME%\lib\security目录下覆盖原来的文件
 *  1. 如果安装了JDK，将两个jar文件放到%JDK_HOME%\jre\lib\security目录下覆盖原来文件
 *
 */
class WXBizMsgCrypt(token: String, encodingAesKey: String, appId: String) {
    var aesKey: ByteArray
    var token: String
    var appId: String

    /**
     * 构造函数
     * @param token 公众平台上，开发者设置的token
     * @param encodingAesKey 公众平台上，开发者设置的EncodingAESKey
     * @param appId 公众平台appid
     *
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    init {
        if (encodingAesKey.length != 43) {
            throw AesException(AesException.ILLEGAL_AES_KEY)
        }
        this.token = token
        this.appId = appId
        aesKey = Base64Utils.decodeFromString("$encodingAesKey=")
    }

    // 生成4个字节的网络字节序
    fun getNetworkBytesOrder(sourceNumber: Int): ByteArray {
        val orderBytes = ByteArray(4)
        orderBytes[3] = (sourceNumber and 0xFF).toByte()
        orderBytes[2] = (sourceNumber shr 8 and 0xFF).toByte()
        orderBytes[1] = (sourceNumber shr 16 and 0xFF).toByte()
        orderBytes[0] = (sourceNumber shr 24 and 0xFF).toByte()
        return orderBytes
    }

    // 还原4个字节的网络字节序
    fun recoverNetworkBytesOrder(orderBytes: ByteArray): Int {
        var sourceNumber = 0
        for (i in 0..3) {
            sourceNumber = sourceNumber shl 8
            sourceNumber = sourceNumber or (orderBytes[i].toInt() and 0xff)
        }
        return sourceNumber
    }

    val randomStr: String
        // 随机生成16位字符串
        get() {
            val base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val random = Random()
            val sb = StringBuffer()
            for (i in 0..15) {
                val number = random.nextInt(base.length)
                sb.append(base[number])
            }
            return sb.toString()
        }

    /**
     * 对明文进行加密.
     *
     * @param text 需要加密的明文
     * @return 加密后base64编码的字符串
     * @throws AesException aes加密失败
     */
    @Throws(AesException::class)
    fun encrypt(text: String, randomStr: String = this.randomStr): String {
        val byteCollector = ByteGroup()
        val randomStrBytes = randomStr.toByteArray(CHARSET)
        val textBytes = text.toByteArray(CHARSET)
        val networkBytesOrder = getNetworkBytesOrder(textBytes.size)
        val appidBytes = appId.toByteArray(CHARSET)

        // randomStr + networkBytesOrder + text + appid
        byteCollector.addBytes(randomStrBytes)
        byteCollector.addBytes(networkBytesOrder)
        byteCollector.addBytes(textBytes)
        byteCollector.addBytes(appidBytes)

        // ... + pad: 使用自定义的填充方式对明文进行补位填充
        val padBytes = encode(byteCollector.size())
        byteCollector.addBytes(padBytes)

        // 获得最终的字节流, 未加密
        val unencrypted = byteCollector.toBytes()
        return try {
            // 设置加密模式为AES的CBC模式
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val keySpec = SecretKeySpec(aesKey, "AES")
            val iv = IvParameterSpec(aesKey, 0, 16)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

            // 加密
            val encrypted = cipher.doFinal(unencrypted)

            // 使用BASE64对加密后的字符串进行编码
            Base64Utils.encodeToString(encrypted)
        } catch (e: Exception) {
            throw AesException(AesException.ENCRYPT_AES_ERROR, e)
        }
    }

    /**
     * 对密文进行解密.
     *
     * @param text 需要解密的密文
     * @return 解密得到的明文
     * @throws AesException aes解密失败
     */
    @Throws(AesException::class)
    fun decrypt(text: String): String {
        val original: ByteArray = try {
            // 设置解密模式为AES的CBC模式
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val keySpec = SecretKeySpec(aesKey, "AES")
            val iv = IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16))
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)

            // 使用BASE64对密文进行解码
            val encrypted = Base64Utils.decodeFromString(text)
            // 解密
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            throw AesException(AesException.DECRYPT_AES_ERROR, e)
        }
        val xmlContent: String
        val fromAppid: String
        try {
            // 去除补位字符
            val bytes = decode(original)

            // 分离16位随机字符串,网络字节序和AppId
            val networkOrder = Arrays.copyOfRange(bytes, 16, 20)
            val xmlLength = recoverNetworkBytesOrder(networkOrder)
            xmlContent = String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET)
            fromAppid = String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.size),
                    CHARSET)
        } catch (e: Exception) {
            throw AesException(AesException.ILLEGAL_BUFFER, e)
        }

        // appid不相同的情况
        if (fromAppid != appId) {
            throw AesException(AesException.VALIDATE_APPID_ERROR)
        }
        return xmlContent
    }

    /**
     * 将公众平台回复用户的消息加密打包.
     *
     *  1. 对要发送的消息进行AES-CBC加密
     *  1. 生成安全签名
     *  1. 将消息密文和安全签名打包成xml格式
     *
     *
     * @param replyMsg 公众平台待回复用户的消息，xml格式的字符串
     * @param timeStamp 时间戳，可以自己生成，也可以用URL参数的timestamp
     * @param nonce 随机串，可以自己生成，也可以用URL参数的nonce
     *
     * @return 加密后的可以直接回复用户的密文，包括msg_signature, timestamp, nonce, encrypt的xml格式的字符串
     * @throws AesException 执行失败，请查看该异常的错误码和具体的错误信息
     */
    @Throws(AesException::class)
    fun encryptMsg(replyMsg: String, nonce: String): EncryptReplyMsg {
        // 加密
        val encrypt = encrypt(randomStr, replyMsg)
        // 生成安全签名
        val timeStamp = System.currentTimeMillis()
        val signature = shaHex(token, timeStamp.toString(), nonce, encrypt)

        // 生成发送的xml
        return EncryptReplyMsg(encrypt, signature, timeStamp, nonce)
    }


    companion object {
        var CHARSET = Charsets.UTF_8
    }
}