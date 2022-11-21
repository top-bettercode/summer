package jpa.unit

import jpa.ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface

/**
 * @author Peter Wu
 */
val repository: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks 数据层"
            +" */"
        }
        implement(
            JavaType("top.bettercode.simpleframework.data.jpa.BaseRepository").typeArgument(
                entityType,
                primaryKeyType
            )
        )

    }
}