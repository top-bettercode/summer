package top.bettercode.summer.tools.lang.util

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES加解密工具
 *
 * @author Frank
 */
object AESUtil {
    const val AES = "AES"
    const val ECB = "ECB"
    const val PKCS5Padding = "PKCS5Padding"

    /**
     * AES加密
     *
     * @param content 内容
     * @param password 密钥
     * @param algorithm 算法
     * @return 加密后数据
     */
    @JvmOverloads
    @JvmStatic
    fun encrypt(content: ByteArray, password: String, algorithm: String = AES): ByteArray {
        val cipher: Cipher = if (algorithm.endsWith("PKCS7Padding")) {
            Cipher.getInstance(algorithm, "BC")
        } else {
            Cipher.getInstance(algorithm)
        }
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"))
        return cipher.doFinal(content)
    }

    /**
     * AES解密
     *
     * @param content 加密内容
     * @param password 密钥
     * @param algorithm 算法
     * @return 解密后数据
     */
    @JvmOverloads
    @JvmStatic
    fun decrypt(content: ByteArray, password: String, algorithm: String = AES): ByteArray {
        val cipher = if (algorithm.endsWith("PKCS7Padding")) {
            Cipher.getInstance(algorithm, "BC")
        } else {
            Cipher.getInstance(algorithm)
        }
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"))
        return cipher.doFinal(content)
    }

    /**
     * AES加密
     *
     * @param content 内容
     * @param password 密钥
     * @param algorithm 算法
     * @param ivStr 向量
     * @return 加密后数据
     */
    @JvmOverloads
    @JvmStatic
    fun encrypt(
        content: ByteArray,
        password: String,
        ivStr: ByteArray,
        algorithm: String = AES
    ): ByteArray {
        val cipher = if (algorithm.endsWith("PKCS7Padding")) {
            Cipher.getInstance(algorithm, "BC")
        } else {
            Cipher.getInstance(algorithm)
        }
        val iv = IvParameterSpec(ivStr)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"), iv)
        return cipher.doFinal(content)
    }

    /**
     * AES解密
     *
     * @param content 加密内容
     * @param password 密钥
     * @param algorithm 算法
     * @param ivStr 向量
     * @return 解密后数据
     */
    @JvmOverloads
    @JvmStatic
    fun decrypt(
        content: ByteArray,
        password: String,
        ivStr: ByteArray,
        algorithm: String = AES
    ): ByteArray {
        val cipher = if (algorithm.endsWith("PKCS7Padding")) {
            Cipher.getInstance(algorithm, "BC")
        } else {
            Cipher.getInstance(algorithm)
        }
        val iv = IvParameterSpec(ivStr)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(password.toByteArray(), "AES"), iv)
        return cipher.doFinal(content)
    }
}