package org.springframework.data.jpa.repository.query;

import java.lang.reflect.Method;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;

/**
 * @author Peter Wu
 */
public class JpaExtQueryMethod extends JpaQueryMethod {

  private final String statementId;
  private final MappedStatement mappedStatement;


  public JpaExtQueryMethod(Method method,
      RepositoryMetadata metadata,
      ProjectionFactory factory,
      QueryExtractor extractor, Configuration configuration) {
    super(method, metadata, factory, extractor);
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

}
