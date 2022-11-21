package top.bettercode.simpleframework.data.jpa.query;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.JpaParametersParameterAccessor;
import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.lang.Nullable;

/**
 * @author Peter Wu
 */
public class JpaQueryLogExecution extends JpaQueryExecution {

  private final JpaQueryExecution delegate;
  private final String id;

  public JpaQueryLogExecution(JpaQueryExecution delegate, String id) {
    this.delegate = delegate;
    this.id = id;
  }

  @Override
  @Nullable
  public Object execute(@NotNull AbstractJpaQuery query, @NotNull JpaParametersParameterAccessor accessor) {
    try {
      MDC.put("id", id);
      return delegate.execute(query, accessor);
    } finally {
      MDC.remove("id");
    }
  }

  @Override
  protected Object doExecute(
      @NotNull AbstractJpaQuery query, @NotNull JpaParametersParameterAccessor accessor) {
    return null;
  }


}
