package top.bettercode.generator.dom.unit

import top.bettercode.generator.SortedProperties
import java.io.File

/**
 *
 * @author Peter Wu
 */
open class PropertiesUnit(
    override val name: String,
    override var replaceable: Boolean = false,
    override val sourceSet: SourceSet = SourceSet.MAIN,
    override val directorySet: DirectorySet = DirectorySet.RESOURCES,
    apply: PropertiesUnit.() -> Unit = { }
) : GenUnit {
    init {
        this.apply(apply)
    }

    private val properties: SortedProperties = SortedProperties()
    override var write: File.() -> Unit = { properties.store(this.outputStream(), null) }

    operator fun set(key: String, value: String) {
        properties[key] = value
    }

    /**
     * @param directory 基础目录
     */
    fun load(directory: File) {
        properties.load(trueFile(directory).inputStream())
    }
}