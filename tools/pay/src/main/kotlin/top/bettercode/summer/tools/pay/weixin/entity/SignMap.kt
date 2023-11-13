package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 *
 * @author Peter Wu
 */
@JacksonXmlRootElement(localName = "xml")
class SignMap : HashMap<String, Any?>() {
    var sign: String?
        get() = this["sign"] as String?
        set(value) {
            this["sign"] = value
        }
}