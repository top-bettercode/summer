package top.bettercode.generator.dom.java

class Annotations(private val annotations: MutableList<String> = mutableListOf()) : MutableList<String> by annotations {
    companion object {
        val regex = Regex("(@[^( ]+)")
        val splitRegex = Regex("[\n ]")
    }

    val needImportedTypes: MutableSet<JavaType> = mutableSetOf()

    private fun calculateAnnotation(annotation: String): String {
        var calculated = annotation
        annotation.split(splitRegex).forEach {
            val groupValues = regex.find(it)?.groupValues
            if (groupValues != null) {
                val typeString = groupValues[1].substringAfter("@")
                val javaType = JavaType(typeString)
                if (javaType.isExplicitlyImported) {
                    needImportedTypes.add(javaType)
                    calculated = calculated.replace("@$typeString", "@${javaType.shortName}")
                }
            }
        }
        return calculated
    }

    override fun add(element: String): Boolean {
        return annotations.add(calculateAnnotation(element))
    }

    override fun add(index: Int, element: String) {
        return annotations.add(index, calculateAnnotation(element))
    }

    override fun addAll(elements: Collection<String>): Boolean {
        return annotations.addAll(elements.map { calculateAnnotation(it) })
    }

    override fun addAll(index: Int, elements: Collection<String>): Boolean {
        return annotations.addAll(index, elements.map { calculateAnnotation(it) })
    }
}
