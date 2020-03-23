package org.springframework.data.jpa.repository.query.mybatis;

import java.lang.reflect.Method;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;

/**
 * @author Peter Wu
 */
public class JpaExtQueryMethod extends JpaQueryMethod {

  private final String statement;
  private final boolean mybatisQuery;

  public JpaExtQueryMethod(Method method,
      RepositoryMetadata metadata,
      ProjectionFactory factory,
      QueryExtractor extractor) {
    super(method, metadata, factory, extractor);
    mybatisQuery = method.isAnnotationPresent(MybatisTemplate.class);
    statement = method.getDeclaringClass().getName() + "." + method.getName();
  }

  public String getStatement() {
    return statement;
  }

  public boolean isMybatisQuery() {
    return mybatisQuery;
  }

}
