/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.bettercode.lang.util

import top.bettercode.lang.util.Hex.encode
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Provides SHA512 digest methods.
 *
 *
 *
 * Based on Commons Codec, which does not presently provide SHA512 support.
 *
 *
 * @author Ben Alex
 * @since 2.0.1
 */
object Sha256DigestUtils {
    /**
     * Returns an SHA digest.
     *
     * @return An SHA digest instance.
     * @throws RuntimeException when a [NoSuchAlgorithmException] is
     * caught.
     */
    private val sha256Digest: MessageDigest by lazy {
        try {
            MessageDigest.getInstance("SHA-256")
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
    fun sha(data: ByteArray?): ByteArray {
        return sha256Digest.digest(data)
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
    fun shaHex(data: ByteArray?): String {
        return String(encode(sha(data)))
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    @JvmStatic
    fun shaHex(data: String): String {
        return String(encode(sha(data)))
    }
}