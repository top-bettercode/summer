package top.bettercode.summer.web.support.gb2260

class InvalidCodeException(s: String?) : RuntimeException(s) {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}