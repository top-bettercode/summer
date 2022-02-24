package top.bettercode.generator.dom.unit

import java.io.File

/**
 *
 * @author Peter Wu
 */
interface GenUnit {
    val name: String
    var replaceable: Boolean
    val sourceSet: SourceSet
        get() = SourceSet.MAIN
    val directorySet: DirectorySet
        get() = DirectorySet.JAVA
    val write: File.() -> Unit
        get() = {}

    val file: File
        get() {
            val file = File(name)
            return if (file.isAbsolute || SourceSet.ROOT == sourceSet) file else {
                File("src/${sourceSet.name.toLowerCase()}/${directorySet.name.toLowerCase()}/$name")
            }
        }

    fun trueFile(directory: File): File {
        return if (file.isAbsolute) file else File(directory, file.path)
    }

    fun writeTo(directory: File) {
        val destFile = trueFile(directory)
        if (!destFile.exists() || !destFile.readLines().any { it.contains("[[Don't cover]]") }) {
            destFile.parentFile.mkdirs()
            println(
                "${if (destFile.exists()) "覆盖" else "生成"}：${file.path}"
            )
            destFile.write()
        }
    }

}

enum class SourceSet {
    MAIN, TEST, ROOT
}

enum class DirectorySet {
    JAVA, KOTLIN, RESOURCES
}
