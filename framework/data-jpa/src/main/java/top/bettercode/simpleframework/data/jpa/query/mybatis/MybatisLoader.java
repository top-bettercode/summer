package top.bettercode.simpleframework.data.jpa.query.mybatis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.custom.CustomLoader;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.transform.ResultTransformer;

/**
 * @author Peter Wu
 */
public class MybatisLoader extends CustomLoader {

  public MybatisLoader(CustomQuery customQuery,
      SessionFactoryImplementor factory) {
    super(customQuery, factory);
  }

  @Override
  protected List<?> getResultList(List results, ResultTransformer resultTransformer)
      throws QueryException {
    if (resultTransformer instanceof MybatisResultTransformer) {
      return results;
    } else {
      return super.getResultList(results, resultTransformer);
    }
  }

  @Override
  public Object loadSingleRow(ResultSet resultSet, SharedSessionContractImplementor session,
      QueryParameters queryParameters, boolean returnProxies) throws HibernateException {
    ResultTransformer resultTransformer = queryParameters.getResultTransformer();
    if (resultTransformer instanceof MybatisResultTransformer) {
      try {
        return ((MybatisResultTransformer) resultTransformer).transform(resultSet);
      } catch (SQLException e) {
        throw new HibernateException(e);
      }
    } else {
      return super.loadSingleRow(resultSet, session, queryParameters, returnProxies);
    }
  }

  @Override
  protected List<?> processResultSet(ResultSet rs, QueryParameters queryParameters,
      SharedSessionContractImplementor session, boolean returnProxies,
      ResultTransformer forcedResultTransformer, int maxRows,
      List<AfterLoadAction> afterLoadActions) throws SQLException {
    ResultTransformer resultTransformer = queryParameters.getResultTransformer();
    if (resultTransformer instanceof MybatisResultTransformer) {
      return ((MybatisResultTransformer) resultTransformer).transformList(rs);
    } else {
      return super.processResultSet(rs, queryParameters, session, returnProxies,
          forcedResultTransformer, maxRows, afterLoadActions);
    }
  }
}
