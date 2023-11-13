package top.bettercode.summer.tools.pay.weixin

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.junit.jupiter.api.Test
import org.springframework.util.DigestUtils
import top.bettercode.summer.tools.pay.weixin.entity.RefundInfo
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 *
 * @author Peter Wu
 */
class WeixinPayClientTest {

    @Test
    fun decryptRefundInfo() {
        // Step 1: Base64 decode the encrypted data
        val encryptedBytes = Base64.getDecoder().decode("WXj7zEJw4dH9qBCY0KUTQNTwdQVKWzrs60mbLKCs/QAUZBLs5Sv+EFgZr61iunt9Qv4I8sSfF9JjFhQZ2wp7uqE6w4GvyDTP95RpsOdjzcrFB9aFX+c1NEYrZgJQKi7N7YU1HLuj4cPPox50srU5eiZRH4BSEpN/R7CeItZUm4TRD+55XLunmVYmkWsFvR9HoUssC06W46Y93V8kn2o1rKs2jtuwc6PK9zTQpr+HdRSzU/FWIDAtVV6NqkUTWFUKHvG8WQgvfexZzh1ta+yT0W21RGfqGF00aRBiyvuihpfO/hXP79kuT0XVCgsYl7eUdDdIcVX7Xnyr9uVDUuyal+6PfVIUglsDuVHClS1/PtiGeBhid8FUtyvhzInMwqU9fcDybOObmRcizI2WWQpk7HkJleXy2JIA9RMaPSsVgjyWDIGOajqE0NmYMmAgqal7MHOatu0pTdayf2blErPzyeNNlp1W+4CPvTmakGB8MeQu5I3PtRV9/5dNwl6uQsEyVF6d3rFxgH+k41uPlzUITdZNoW72scaR/Rlh8EXUhd7PjJTdu0I6ZX5YcVH0cZMHY8BW/C5DXv1VwI37DjrdWFmbL7ncr1njZFEY6QpuzJ9GXjHiDmg+hcxh1NhUFYVka58qROcO1SSu49bHGN29auHvaLX8dHDBe+TOVr3c/ohqIPa2x+oQ4wPNN1MElkbD2qvPmw9SgeLIhegz7bVcy6B5A7/CGfTlc9HlMmr639b0qbPctiqiRSnPP105jix9+0dDIe/psJ8Lb92zIcYH1z1LMJCBOSjdGNS1bAkeKieQAJKnzlBUf6W6/xnqou+WoxoK6InDC3k97sIifigt8udvk+l9haEnTnCO9rgQawuaaK9AqtY9ESt4JkE4JRfiYt64lGJybnhL+B0l14J6+tpGEvicVaNtk4wMNHQQW4f79g+EcyV+3d7d17K6Rjyqu3MJYChJVZW7OYeoiWIm2qIzsEL7Iel/Ts1FEGs/jQWSSBMTylt6MKIOP6zZKB6glW/MFFbJw4Y8XnJMLWFNeEHWgPz99SZbftSmdk4FHxkdqvY2/W2r4vLskW0rNkenPdX7LuQAKfxoIQT6knEFnZAXjKMIpUMlOQ35vdderEWGk3p3yY2xczjHZf2j3nEc")

        // Step 2: Generate MD5 hash of the merchant key
        val keyBytes = DigestUtils.md5DigestAsHex("".toByteArray(charset("UTF-8")))
        System.err.println(keyBytes)
        // Step 3: Perform AES-256-ECB decryption
        val keySpec = SecretKeySpec(keyBytes.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        System.err.println(String(decryptedBytes))
        val readValue = XmlMapper().readValue(decryptedBytes, RefundInfo::class.java)
        System.err.println(readValue)
    }
}