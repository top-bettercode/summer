package top.bettercode.summer.gradle.plugin.jenkins

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class JenkinsDecryptTest {

    @Disabled
    @Test
    fun decrypt() {
        val jenkinsDir = ""
        decrypt(File(jenkinsDir), File(jenkinsDir, "jenkins.plugins.publish_over_ssh.BapSshPublisherPlugin.xml"))
    }

    fun decrypt(jenkinsDir: File, encryptFile: File) {

        // 创建SAXBuilder
        val saxBuilder = SAXBuilder()
        val document: Document = saxBuilder.build(encryptFile)

        // 找到要更新的字段，例如将第一个item的价格字段修改为15.99
        val rootElement: Element = document.rootElement

        decrypt(jenkinsDir, rootElement)

        // 将修改后的XML写回文件
        val outputter = XMLOutputter(Format.getPrettyFormat())
        val decryptFile = File(encryptFile.parent, encryptFile.nameWithoutExtension + "-decrypt." + encryptFile.extension)
        outputter.output(document, decryptFile.outputStream())
    }

    fun decrypt(jenkinsDir: File, element: Element) {
        if (element.name == "secretPassword" || element.name == "secretPassphrase") {
            val encryptStr = element.text
            val decryptValue = decrypt(File(jenkinsDir, "secrets"), encryptStr)
            element.text = decryptValue
        }
        element.content.filterIsInstance<Element>().forEach { content ->
            decrypt(jenkinsDir, content)
        }
    }

    fun decrypt(secretsDir: File, encryptStr: String): String {
        val payload: ByteArray = Base64.getDecoder().decode(encryptStr.substring(1, encryptStr.length - 1))
        when (payload[0]) {
            1.toByte() -> {
                // For PAYLOAD_V1 we use this byte shifting model, V2 probably will need DataOutput
                val ivLength = (payload[1].toInt() and 0xff shl 24
                        or (payload[2].toInt() and 0xff shl 16)
                        or (payload[3].toInt() and 0xff shl 8)
                        or (payload[4].toInt() and 0xff))
                val dataLength = (payload[5].toInt() and 0xff shl 24
                        or (payload[6].toInt() and 0xff shl 16)
                        or (payload[7].toInt() and 0xff shl 8)
                        or (payload[8].toInt() and 0xff))
                if (payload.size != 1 + 8 + ivLength + dataLength) {
                    // not valid v1
                    throw IllegalArgumentException("Invalid payload length")
                }
                val iv = Arrays.copyOfRange(payload, 9, 9 + ivLength)
                val cipher = getCipher(ALGORITHM)
                val secret: SecretKey
                val payloadKey = load(secretsDir)
                Assertions.assertNotNull(payloadKey, "secrets not found")
                // Due to the stupid US export restriction JDK only ships 128bit version.
                secret = SecretKeySpec(payloadKey, 0, 128 / 8, KEY_ALGORITHM)

                cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))

                val code = Arrays.copyOfRange(payload, 9 + ivLength, payload.size)
                return String(cipher.doFinal(code), StandardCharsets.UTF_8)
            }

            else -> throw IllegalArgumentException("Unknown payload version")
        }
    }

    private fun load(secretsDir: File): ByteArray? {
        val f = File(secretsDir, "hudson.util.Secret")
        if (!f.exists()) return null
        val sym = getCipher("AES")
        val file = File(secretsDir, "master.key")
        val masterKey = toAes128Key(read(file).trim { it <= ' ' })

        sym.init(Cipher.DECRYPT_MODE, masterKey)
        Files.newInputStream(f.toPath()).use { fis ->
            CipherInputStream(fis, sym).use { cis ->
                val bytes = cis.readBytes()
                return verifyMagic(bytes)
            }
        }
    }

    fun read(file: File): String {
        val out = StringWriter()
        val w = PrintWriter(out)
        val `in` = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)
        var line: String?
        while (`in`.readLine().also { line = it } != null) w.println(line)
        return out.toString()
    }

    fun randomBytes(size: Int): ByteArray {
        val random = ByteArray(size)
        SecureRandom().nextBytes(random)
        return random
    }

    private fun verifyMagic(payload: ByteArray): ByteArray? {
        val payloadLen = payload.size - MAGIC.size
        if (payloadLen < 0) return null // obviously broken
        for (i in MAGIC.indices) {
            if (payload[payloadLen + i] != MAGIC[i]) return null // broken
        }
        val truncated = ByteArray(payloadLen)
        System.arraycopy(payload, 0, truncated, 0, truncated.size)
        return truncated
    }

    companion object {
        private const val KEY_ALGORITHM = "AES"
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private val MAGIC = "::::MAGIC::::".toByteArray()


        fun getCipher(algorithm: String): Cipher {
            return Cipher.getInstance(algorithm)
        }

        fun toHexString(bytes: ByteArray): String {
            val start = 0
            val len = bytes.size
            val buf = StringBuilder()
            for (i in 0 until len) {
                val b = bytes[start + i].toInt() and 0xFF
                if (b < 16) buf.append('0')
                buf.append(Integer.toHexString(b))
            }
            return buf.toString()
        }

        fun toAes128Key(s: String): SecretKey {
            // turn secretKey into 256 bit hash
            val digest = MessageDigest.getInstance("SHA-256")
            digest.reset()
            digest.update(s.toByteArray(StandardCharsets.UTF_8))

            // Due to the stupid US export restriction JDK only ships 128bit version.
            return SecretKeySpec(digest.digest(), 0, 128 / 8, "AES")
        }
    }
}
