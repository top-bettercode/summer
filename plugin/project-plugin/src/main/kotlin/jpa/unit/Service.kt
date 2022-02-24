import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 * @author Peter Wu
 */
val service: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        annotation("@org.springframework.stereotype.Service")
        javadoc {
            +"/**"
            +" * $remarks 服务层实现"
            +" */"
        }
        superClass =
            JavaType("top.bettercode.simpleframework.data.jpa.BaseService").typeArgument(
                entityType,
                primaryKeyType,
                repositoryType
            )


        //constructor
        constructor(Parameter("repository", repositoryType)) {
            +"super(repository);"
        }
    }
}

