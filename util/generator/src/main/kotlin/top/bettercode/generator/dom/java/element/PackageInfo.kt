package top.bettercode.generator.dom.java.element

import top.bettercode.generator.dom.java.JavaType
import java.io.File


/**
 * The Class Interface.
 *
 * @author Jeff Butler
 */
class PackageInfo(
    val type: JavaType,
    override val canCover: Boolean = false,
    override val isResourcesFile: Boolean = false,
    override val isTestFile: Boolean = false
) : FileUnit(
    "${type.fullyQualifiedNameWithoutTypeParameters.replace(".", File.separator)}.java",
    canCover,
    isTestFile,
    isResourcesFile
)
