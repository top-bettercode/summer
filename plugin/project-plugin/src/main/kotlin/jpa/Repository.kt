import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class Repository : ModuleJavaGenerator() {

    override val type: JavaType
        get() = repositoryType

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 数据层"
                +" * mybatis 模板方法建议统一使用注解{@link org.springframework.data.jpa.repository.query.mybatis.MybatisTemplate},查询前缀使用select，不要使用find"
                +" */"
            }
            val superInterface =
                JavaType("cn.bestwu.simpleframework.data.jpa.BaseRepository").typeArgument(
                    entityType,
                    primaryKeyType
                )
            implement(
                superInterface,
                JavaType("org.springframework.data.querydsl.binding.QuerydslBinderCustomizer").typeArgument(
                    queryDslType
                )
            )

            method(
                "customize",
                JavaType.voidPrimitiveInstance,
                Parameter(
                    "bindings",
                    JavaType("org.springframework.data.querydsl.binding.QuerydslBindings")
                ),
                Parameter(entityName, queryDslType)
            ) {
                annotation("@Override")
                isDefault = true

                columns.forEach {
                    if (it.javaName.contains("password", true)) {
                        +"bindings.excluding($entityName.${it.javaName});"
                    }
                    if (it.javaType == JavaType.stringInstance) {
                        +"//bindings.bind($entityName.${it.javaName}).first(StringExpression::contains);"
                    }
                }
                +"//bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);"
            }
        }
    }
}