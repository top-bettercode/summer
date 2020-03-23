package cn.bestwu.lang.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Sha1加密工具类
 *
 * @author Peter Wu
 */
object Sha1DigestUtil {

    /**
     * Returns an SHA digest.
     *
     * @return An SHA digest instance.
     * @throws RuntimeException when a [NoSuchAlgorithmException] is caught.
     */
    private val sha1Digest: MessageDigest
        get() {
            try {
                return MessageDigest.getInstance("SHA-1")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e.message)
            }

        }

    /**
     * Calculates the SHA digest and returns the value as a `byte[]`.
     *
     * @param data Data to digest
     * @return SHA digest
     */
    @JvmStatic
    fun sha(data: ByteArray): ByteArray {
        return sha1Digest.digest(data)
    }

    /**
     * Calculates the SHA digest and returns the value as a `byte[]`.
     *
     * @param data Data to digest
     * @return SHA digest
     */
    @JvmStatic
    fun sha(data: String): ByteArray {
        return sha(data.toByteArray())
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    @JvmStatic
    fun shaHex(data: ByteArray): String {
        return String(Hex.encode(sha(data)))
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    @JvmStatic
    fun shaHex(data: String): String {
        return String(Hex.encode(sha(data)))
    }

}