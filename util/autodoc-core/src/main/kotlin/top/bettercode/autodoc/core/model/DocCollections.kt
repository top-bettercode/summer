package top.bettercode.autodoc.core.model

class DocCollections : LinkedHashMap<String, LinkedHashSet<String>>() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
