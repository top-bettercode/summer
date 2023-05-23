package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass

/**
 * @author Peter Wu
 */
val service: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        annotation("@org.springframework.stereotype.Service")
        javadoc {
            +"/** $remarks 服务层实现 */"
        }
        superClass =
            JavaType("top.bettercode.summer.data.jpa.BaseService").typeArgument(
                entityType,
                primaryKeyType,
                repositoryType
            )

        if (interfaceService)
            implement(iserviceType)

        //constructor
        constructor(Parameter("repository", repositoryType)) {
            +"super(repository);"
        }
    }
}

val iservice: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks 服务层接口 */"
        }
        val superInterface =
            JavaType("top.bettercode.summer.data.jpa.IBaseService").typeArgument(
                entityType,
                primaryKeyType,
                repositoryType
            )
        implement(superInterface)
    }
}
