package top.bettercode.summer.tools.recipe.material

/**
 *
 * @author Peter Wu
 */
class RecipeMaterials(materials: List<IRecipeMaterial>) : HashMap<String, IRecipeMaterial>() {

    init {
        materials.forEach {
            put(it.id, it)
        }
    }

    /**
     * 硫酸
     */
    val sulfuricAcid = materials.find { it.type == RecipeMaterialType.SULFURIC_ACID }

    /**
     * 液氨
     */
    val liquidAmmonia = materials.find { it.type == RecipeMaterialType.LIQUID_AMMONIA }

    /**
     * 碳铵
     */
    val ammoniumCarbonate = materials.find {
        it.type == RecipeMaterialType.AMMONIUM_CARBONATE
    }

    @JvmName("mutableIterator")
    operator fun iterator(): MutableIterator<IRecipeMaterial> = values.iterator()

}