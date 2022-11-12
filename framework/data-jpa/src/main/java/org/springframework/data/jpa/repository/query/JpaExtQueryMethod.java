package org.springframework.data.jpa.repository.query;

import java.lang.reflect.Method;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.util.Lazy;
import top.bettercode.simpleframework.data.jpa.support.FindFirst;

/**
 * @author Peter Wu
 */
public class JpaExtQueryMethod extends JpaQueryMethod {

  private final String statementId;
  private final MappedStatement mappedStatement;
  private final Lazy<FindFirst> findFirst;


  public JpaExtQueryMethod(Method method,
      RepositoryMetadata metadata,
      ProjectionFactory factory,
      QueryExtractor extractor, Configuration configuration) {
    super(method, metadata, factory, extractor);
    this.findFirst = Lazy.of(
        () -> AnnotatedElementUtils.findMergedAnnotation(method, FindFirst.class));
    this.statementId = method.getDeclaringClass().getName() + "." + method.getName();
    MappedStatement mappedStatement;
    try {
      mappedStatement = configuration.getMappedStatement(this.statementId);
    } catch (Exception e) {
      mappedStatement = null;
    }
    this.mappedStatement = mappedStatement;
  }

  public MappedStatement getMappedStatement() {
    return mappedStatement;
  }

  public String getStatementId() {
    return statementId;
  }

  public boolean isFindFirstQuery() {
    return findFirst.getNullable() != null;
  }

}
