package top.bettercode.summer.tools.generator.dom.unit

import top.bettercode.summer.tools.generator.SortedProperties
import java.io.File

/**
 *
 * @author Peter Wu
 */
open class PropertiesUnit(
        override val name: String,
        override var overwrite: Boolean = false,
        override val sourceSet: SourceSet = SourceSet.MAIN,
        override val directorySet: DirectorySet = DirectorySet.RESOURCES
) : GenUnit {
    private val properties: SortedProperties = SortedProperties(true)
    override var write: File.(String) -> Boolean = {
        if (!properties.isEmpty || it.isNotBlank()) {
            properties.store(this.outputStream(), null)
            readText() != it
        } else {
            false
        }
    }

    operator fun set(key: String, value: String) {
        properties[key] = value
    }

    operator fun get(key: String): String? {
        return properties[key] as String?
    }

    fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }

    /**
     * @param directory 基础目录
     */
    fun load(directory: File) {
        val trueFile = outputFile(directory)
        if (trueFile.exists()) {
            properties.load(trueFile.inputStream())
        }
    }
}