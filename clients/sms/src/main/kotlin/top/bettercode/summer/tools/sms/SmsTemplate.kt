package top.bettercode.summer.tools.sms

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import top.bettercode.summer.logging.annotation.LogMarker
import top.bettercode.summer.tools.lang.client.ApiTemplate

/**
 * @author Peter Wu
 */
@LogMarker(SmsTemplate.LOG_MARKER_STR)
abstract class SmsTemplate : ApiTemplate {
    constructor(collectionName: String, name: String, logMarker: String, connectTimeout: Int,
                readTimeout: Int) : super(collectionName, name, logMarker, connectTimeout, readTimeout, null, null)

    constructor(collectionName: String, name: String, logMarker: String, connectTimeout: Int,
                readTimeout: Int, requestDecrypt: ((ByteArray) -> ByteArray)?,
                responseDecrypt: ((ByteArray) -> ByteArray)?) : super(collectionName, name, logMarker, connectTimeout, readTimeout, requestDecrypt,
            responseDecrypt)

    companion object {
        const val LOG_MARKER_STR = "sms"

        @JvmField
        val LOG_MARKER: Marker? = MarkerFactory.getMarker(LOG_MARKER_STR)
    }
}
