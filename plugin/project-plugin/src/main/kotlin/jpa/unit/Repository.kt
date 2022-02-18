package jpa.unit

import ModuleJavaGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface

/**
 * @author Peter Wu
 */
val repository: ModuleJavaGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
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