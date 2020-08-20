package cn.bestwu.logging.logback

import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.html.CssBuilder

class CustCssBuilder : CssBuilder {
    override fun addCss(sbuf: StringBuilder) {
        sbuf.append("<style  type=\"text/css\">")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("table { width: 100%; overflow: hidden; table-layout：fixed; font-size: small; margin:0px auto; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TR.even { background: #FFFFFF; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TR.odd { background: #EAEAEA; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TR.warn TD.Level, TR.error TD.Level, TR.fatal TD.Level {font-weight: bold; color: #FF4040; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD { max-width: 1900px }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Time, TD.Date { text-align: right; font-family: courier, monospace; font-size: smaller; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Thread { text-align: left; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Level { text-align: right; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Logger { text-align: left; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TR.header { background: #555555; color: #FFF; font-weight: bold; font-size: larger; }")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Warn { background: rgba(255,255,0,0.3); font-family: courier, monospace;}")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("TD.Exception { background: rgba(255,0,0,0.3); font-family: courier, monospace;}")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("pre { white-space: pre-wrap; word-wrap: break-word;}")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
        sbuf.append("</style>")
        sbuf.append(CoreConstants.LINE_SEPARATOR)
    }
}