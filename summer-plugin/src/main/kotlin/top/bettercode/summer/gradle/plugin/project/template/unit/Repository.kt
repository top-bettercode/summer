package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.Interface

/**
 * @author Peter Wu
 */
val repository: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks 数据层 */"
        }
        implement(
                JavaType("top.bettercode.summer.data.jpa.BaseRepository").typeArgument(
                        entityType,
                        primaryKeyType
                )
        )

    }
}