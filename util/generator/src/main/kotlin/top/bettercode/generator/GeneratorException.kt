package top.bettercode.generator

/**
 *
 * @author Peter Wu
 */
class GeneratorException(override val message: String? = null) : Exception() {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}