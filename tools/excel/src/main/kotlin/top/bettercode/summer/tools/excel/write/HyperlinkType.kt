package top.bettercode.summer.tools.excel.write

enum class HyperlinkType {
    /**
     * Link to an existing file or web page
     */
    URL,

    /**
     * Link to a place in this document
     */
    DOCUMENT,

    /**
     * Link to an E-mail address
     */
    EMAIL,

    /**
     * Link to a file
     */
    FILE
}
