package top.bettercode.summer.tools.weixin.support.offiaccount.aes

class AesException internal constructor(
        @JvmField val code: Int, exception: Exception? = null) : Exception(getMessage(code), exception) {

    companion object {
        const val OK = 0
        const val VALIDATE_SIGNATURE_ERROR = -40001
        const val PARSE_XML_ERROR = -40002
        const val COMPUTE_SIGNATURE_ERROR = -40003
        const val ILLEGAL_AES_KEY = -40004
        const val VALIDATE_APPID_ERROR = -40005
        const val ENCRYPT_AES_ERROR = -40006
        const val DECRYPT_AES_ERROR = -40007
        const val ILLEGAL_BUFFER = -40008

        //const val EncodeBase64Error = -40009;
        //const val DecodeBase64Error = -40010;
        //const val GenReturnXmlError = -40011;
        private fun getMessage(code: Int): String? {
            return when (code) {
                VALIDATE_SIGNATURE_ERROR -> "签名验证错误"
                PARSE_XML_ERROR -> "xml解析失败"
                COMPUTE_SIGNATURE_ERROR -> "sha加密生成签名失败"
                ILLEGAL_AES_KEY -> "SymmetricKey非法"
                VALIDATE_APPID_ERROR -> "appid校验失败"
                ENCRYPT_AES_ERROR -> "aes加密失败"
                DECRYPT_AES_ERROR -> "aes解密失败"
                ILLEGAL_BUFFER -> "解密后得到的buffer非法"
                else -> null // cannot be
            }
        }
    }
}
