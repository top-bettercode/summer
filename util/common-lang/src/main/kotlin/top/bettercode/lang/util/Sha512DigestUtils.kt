package top.bettercode.lang.util

import top.bettercode.lang.util.Hex.encode
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha512DigestUtils {
    /**
     * Returns an SHA digest.
     * @return An SHA digest instance.
     * @throws RuntimeException when a [java.security.NoSuchAlgorithmException] is
     * caught.
     */
    private val sha512Digest: MessageDigest
        get() = try {
            MessageDigest.getInstance("SHA-512")
        } catch (ex: NoSuchAlgorithmException) {
            throw RuntimeException(ex.message)
        }


    /**
     * Calculates the SHA digest and returns the value as a `byte[]`.
     * @param data Data to digest
     * @return SHA digest
     */
    @JvmStatic
    fun sha(data: ByteArray?): ByteArray {
        return sha512Digest.digest(data)
    }

    /**
     * Calculates the SHA digest and returns the value as a `byte[]`.
     * @param data Data to digest
     * @return SHA digest
     */
    @JvmStatic
    fun sha(data: String): ByteArray {
        return sha(data.toByteArray())
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    @JvmStatic
    fun shaHex(data: ByteArray?): String {
        return String(encode(sha(data)))
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    @JvmStatic
    fun shaHex(data: String): String {
        return String(encode(sha(data)))
    }
}