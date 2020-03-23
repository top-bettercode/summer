package cn.bestwu.lang.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * @author Peter Wu
 */
object AESUtil {

    /**
     * AES加密
     *
     * @param content 待加密的字符串
     * @param encryptKey 加密密钥
     * @return 加密后的字符串
     * @throws Exception Exception
     */
    @JvmStatic
    @Throws(Exception::class)
    fun encrypt(content: String, encryptKey: String): String {
        val kgen = KeyGenerator.getInstance("AES")
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(encryptKey.toByteArray())
        kgen.init(128, random)

        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(kgen.generateKey().encoded, "AES"))

        return parseByte2HexStr(cipher.doFinal(content.toByteArray(charset("UTF-8"))))
    }

    /**
     * AES解密
     *
     * @param encryptedStr 待解密的字符串
     * @param decryptKey 解密密钥
     * @return 解密后的字符串
     * @throws Exception Exception
     */
    @JvmStatic
    @Throws(Exception::class)
    fun decrypt(encryptedStr: String, decryptKey: String): String {
        val kgen = KeyGenerator.getInstance("AES")
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(decryptKey.toByteArray())
        kgen.init(128, random)

        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(kgen.generateKey().encoded, "AES"))
        val decryptBytes = cipher.doFinal(parseHexStr2Byte(encryptedStr)!!)

        return String(decryptBytes)
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr 16进制
     * @return 二进制
     */
    @JvmStatic
    fun parseHexStr2Byte(hexStr: String): ByteArray? {
        if (hexStr.isEmpty()) {
            return null
        }
        val result = ByteArray(hexStr.length / 2)
        for (i in 0 until hexStr.length / 2) {
            val high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16)
            val low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16)
            result[i] = (high * 16 + low).toByte()
        }
        return result
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf 二进制
     * @return 16进制
     */
    @JvmStatic
    fun parseByte2HexStr(buf: ByteArray): String {
        val sb = StringBuilder()
        for (aBuf in buf) {
            var hex = Integer.toHexString((aBuf and 0xFF.toByte()).toInt())
            if (hex.length == 1) {
                hex = "0$hex"
            }
            sb.append(hex.toUpperCase())
        }
        return sb.toString()
    }
}