package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
class RecipeMaterials(materials: List<IRecipeMaterial> = emptyList()) : HashMap<String, IRecipeMaterial>() {

    init {
        materials.forEach {
            put(it.id, it)
        }
    }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<IRecipeMaterial> = values.iterator()

}