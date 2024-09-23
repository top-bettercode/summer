package top.bettercode.summer.tools.hikvision

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 签名工具
 */
object SignUtil {
    //请求Body内容MD5 Header
    const val HTTP_HEADER_CONTENT_MD5: String = "Content-MD5"

    //签名Header
    const val X_CA_SIGNATURE: String = "x-ca-signature"

    //所有参与签名的Header
    const val X_CA_SIGNATURE_HEADERS: String = "x-ca-signature-headers"

    //请求时间戳
    const val X_CA_TIMESTAMP: String = "x-ca-timestamp"

    //请求放重放Nonce,15分钟内保持唯一,建议使用UUID
    const val X_CA_NONCE: String = "x-ca-nonce"

    //APP KEY
    const val X_CA_KEY: String = "x-ca-key"

    //签名算法HmacSha256
    const val HMAC_SHA256: String = "HmacSHA256"

    //编码UTF-8
    const val ENCODING: String = "UTF-8"

    //UserAgent
    const val USER_AGENT: String = "demo/aliyun/java"

    //换行符
    const val LF: String = "\n"

    //串联符
    const val SPE1: String = ","

    //示意符
    const val SPE2: String = ":"

    //连接符
    const val SPE3: String = "&"

    //赋值符
    const val SPE4: String = "="

    //问号符
    const val SPE5: String = "?"

    //默认请求超时时间,单位毫秒
    var DEFAULT_TIMEOUT: Int = 1000

    //参与签名的系统Header前缀,只有指定前缀的Header才会参与到签名中
    const val CA_HEADER_TO_SIGN_PREFIX_SYSTEM: String = "x-ca-"

    const val JDK_VERSION: Double = 1.7


    @JvmStatic
    fun sign(
        headers: HttpHeaders, method: String, path: String,
        properties: HikvisionProperties
    ) {
        headers.add(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        headers.add(
            X_CA_TIMESTAMP,  //        "1726810297925");
            Date().time.toString()
        )
        headers.add(
            X_CA_NONCE,  //        "5f50916a-0856-4bab-98a5-357cfd880157");
            UUID.randomUUID().toString()
        )
        headers.add(X_CA_KEY, properties.appKey)

        val signature: String
        try {
            val secret = properties.appSecret
            val hmacSha256 = Mac.getInstance(HMAC_SHA256)
            val keyBytes = secret.toByteArray(charset(ENCODING))
            hmacSha256.init(SecretKeySpec(keyBytes, 0, keyBytes.size, HMAC_SHA256))

            signature = String(
                Base64.getEncoder().encode(
                    hmacSha256.doFinal(
                        buildStringToSign(method, path, headers, null, null, null)
                            .toByteArray(charset(ENCODING))
                    )
                ),
                charset(ENCODING)
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }

        headers.add(X_CA_SIGNATURE, signature)
    }

    /**
     * 构建待签名字符串
     */
    private fun buildStringToSign(
        method: String, path: String,
        headers: HttpHeaders?,
        querys: Map<String, String>?,
        bodys: Map<String, String>?,
        signHeaderPrefixList: MutableList<String>?
    ): String {
        val sb = StringBuilder()

        sb.append(method.uppercase(Locale.getDefault())).append(LF)
        if (null != headers) {
            val accept = headers[HttpHeaders.ACCEPT]
            if (!CollectionUtils.isEmpty(accept)) {
                sb.append(java.lang.String.join(",", accept))
                sb.append(LF)
            }

            val contentMd5 = headers[HTTP_HEADER_CONTENT_MD5]
            if (!CollectionUtils.isEmpty(contentMd5)) {
                sb.append(java.lang.String.join(",", contentMd5))
                sb.append(LF)
            }

            val contentType = headers[HttpHeaders.CONTENT_TYPE]
            if (!CollectionUtils.isEmpty(contentType)) {
                sb.append(java.lang.String.join(",", contentType))
                sb.append(LF)
            }

            val date = headers[HttpHeaders.DATE]
            if (!CollectionUtils.isEmpty(date)) {
                sb.append(java.lang.String.join(",", date))
                sb.append(LF)
            }
        }
        sb.append(buildHeaders(headers, signHeaderPrefixList))
        sb.append(buildResource(path, querys, bodys))
        return sb.toString()
    }

    /**
     * 构建待签名Path+Query+BODY
     * @return 待签名
     */
    private fun buildResource(
        path: String, querys: Map<String, String>?,
        bodys: Map<String, String>?
    ): String {
        val sb = StringBuilder()

        if (StringUtils.hasText(path)) {
            sb.append(path)
        }
        val sortMap: MutableMap<String, String> = TreeMap()
        if (null != querys) {
            for ((key, value) in querys) {
                if (StringUtils.hasText(key)) {
                    sortMap[key] = value
                }
            }
        }

        if (null != bodys) {
            for ((key, value) in bodys) {
                if (StringUtils.hasText(key)) {
                    sortMap[key] = value
                }
            }
        }

        val sbParam = StringBuilder()
        for ((key, value) in sortMap) {
            if (StringUtils.hasText(key)) {
                if (sbParam.isNotEmpty()) {
                    sbParam.append(SPE3)
                }
                sbParam.append(key)
                if (StringUtils.hasText(value)) {
                    sbParam.append(SPE4).append(value)
                }
            }
        }
        if (sbParam.isNotEmpty()) {
            sb.append(SPE5)
            sb.append(sbParam)
        }

        return sb.toString()
    }

    /**
     * 构建待签名Http头
     *
     * @param headers 请求中所有的Http头
     * @param signHeaderPrefixList 自定义参与签名Header前缀
     * @return 待签名Http头
     */
    private fun buildHeaders(
        headers: HttpHeaders?,
        signHeaderPrefixList: MutableList<String>?
    ): String {
        val sb = StringBuilder()

        if (null != signHeaderPrefixList) {
            signHeaderPrefixList.remove(X_CA_SIGNATURE)
            signHeaderPrefixList.remove(HttpHeaders.ACCEPT)
            signHeaderPrefixList.remove(HTTP_HEADER_CONTENT_MD5)
            signHeaderPrefixList.remove(HttpHeaders.CONTENT_TYPE)
            signHeaderPrefixList.remove(HttpHeaders.DATE)
            signHeaderPrefixList.sort()
        }
        if (null != headers) {
            val sortMap: Map<String, String> = TreeMap(headers.toSingleValueMap())
            val signHeadersStringBuilder = StringBuilder()
            for ((key, value) in sortMap) {
                if (isHeaderToSign(key, signHeaderPrefixList)) {
                    sb.append(key)
                    sb.append(SPE2)
                    if (StringUtils.hasText(value)) {
                        sb.append(value)
                    }
                    sb.append(LF)
                    if (signHeadersStringBuilder.isNotEmpty()) {
                        signHeadersStringBuilder.append(SPE1)
                    }
                    signHeadersStringBuilder.append(key)
                }
            }
            headers.add(X_CA_SIGNATURE_HEADERS, signHeadersStringBuilder.toString())
        }

        return sb.toString()
    }

    /**
     * Http头是否参与签名 return
     */
    private fun isHeaderToSign(headerName: String, signHeaderPrefixList: List<String>?): Boolean {
        if (io.micrometer.core.instrument.util.StringUtils.isBlank(headerName)) {
            return false
        }

        if (headerName.startsWith(CA_HEADER_TO_SIGN_PREFIX_SYSTEM)) {
            return true
        }

        if (null != signHeaderPrefixList) {
            for (signHeaderPrefix in signHeaderPrefixList) {
                if (headerName.equals(signHeaderPrefix, ignoreCase = true)) {
                    return true
                }
            }
        }

        return false
    }
}
