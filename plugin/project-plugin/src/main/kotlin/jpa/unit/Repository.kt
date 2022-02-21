package jpa.unit

import ProjectGenerator
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
            +" * <p>"
            +" * mybatis 模板方法建议统一使用注解{@link org.springframework.data.jpa.repository.query.mybatis.MybatisTemplate}"
            +" * <p>"
            +" * mybatis 查询方法名注意避免使用以下关键字："
            +" * <p>"
            +" * find…By, read…By, get…By, query…By, search…By, stream…By"
            +" * <p>"
            +" * exists…By"
            +" * <p>"
            +" * count…By"
            +" * <p>"
            +" * delete…By, remove…By"
            +" * <p>"
            +" * …First<number>…,…Top<number>…"
            +" * <p>"
            +" * …Distinct…"
            +" * <p>"
            +" * also see: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-keywords"
            +" *"
            +" * </p>"
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