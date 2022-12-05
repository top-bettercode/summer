package top.bettercode.summer.tools.generator.dom.unit

import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 */
interface GenUnit {
    val name: String
    var overwrite: Boolean
    val sourceSet: SourceSet
        get() = SourceSet.MAIN
    val directorySet: DirectorySet
        get() = DirectorySet.JAVA
    val write: File.(String) -> Boolean
        get() = { false }

    val file: File
        get() {
            val file = File(name)
            return if (file.isAbsolute || SourceSet.ROOT == sourceSet) file else {
                File(
                    "src/${sourceSet.name.lowercase(Locale.getDefault())}/${
                        directorySet.name.lowercase(
                            Locale.getDefault()
                        )
                    }/$name"
                )
            }
        }

    fun outputFile(directory: File? = null): File {
        return if (directory == null || file.isAbsolute) file else File(directory, file.path)
    }

    fun writeTo(directory: File? = null) {
        val destFile = outputFile(directory)
        val exists = destFile.exists()
        if (!exists ||
            (overwrite && !destFile.readLines().any { it.contains("[[Don't cover]]") })
        ) {
            destFile.parentFile.mkdirs()
            val oldContent = if (exists) destFile.readText() else ""
            val writed = destFile.write(oldContent)
            if (writed) {
                if (exists) {
                    println("覆盖：${file.path}")
                } else {
                    println("生成：${file.path}")
                }
            }
        }
    }

}




