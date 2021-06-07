import top.bettercode.generator.dom.java.JavaType

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
            implement(
                JavaType("top.bettercode.simpleframework.data.jpa.BaseRepository").typeArgument(
                    entityType,
                    primaryKeyType
                )
            )

        }
    }
}