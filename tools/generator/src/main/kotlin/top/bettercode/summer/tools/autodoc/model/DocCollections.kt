package top.bettercode.summer.tools.autodoc.model

class DocCollections : LinkedHashMap<String, LinkedHashSet<String>>() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
