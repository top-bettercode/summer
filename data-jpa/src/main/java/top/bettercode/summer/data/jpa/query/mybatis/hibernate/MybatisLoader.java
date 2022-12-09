package top.bettercode.summer.data.jpa.query.mybatis.hibernate;

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
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultTransformer;

/**
 * @author Peter Wu
 */
public class MybatisLoader extends CustomLoader {

  private boolean mybatisQuery;

  public MybatisLoader(CustomQuery customQuery, SessionFactoryImplementor factory) {
    super(customQuery, factory);
  }

  @Override
  protected List<?> getResultList(List results, ResultTransformer resultTransformer)
      throws QueryException {
    if (mybatisQuery) {
      return results;
    } else {
      return super.getResultList(results, resultTransformer);
    }
  }

  @Override
  public Object loadSingleRow(ResultSet resultSet, SharedSessionContractImplementor session,
      QueryParameters queryParameters, boolean returnProxies) throws HibernateException {
    if (mybatisQuery) {
      return ((MybatisResultTransformer) queryParameters.getResultTransformer()).transformResultSet(
          resultSet);
    } else {
      return super.loadSingleRow(resultSet, session, queryParameters, returnProxies);
    }
  }

  @Override
  protected List<?> processResultSet(ResultSet rs, QueryParameters queryParameters,
      SharedSessionContractImplementor session, boolean returnProxies,
      ResultTransformer forcedResultTransformer, int maxRows,
      List<AfterLoadAction> afterLoadActions) throws SQLException {
    if (mybatisQuery) {
      return ((MybatisResultTransformer) queryParameters.getResultTransformer()).transformListResultSet(rs,
          maxRows);
    } else {
      return super.processResultSet(rs, queryParameters, session, returnProxies,
          forcedResultTransformer, maxRows, afterLoadActions);
    }
  }

  @Override
  protected SqlStatementWrapper executeQueryStatement(QueryParameters queryParameters,
      boolean scroll, List<AfterLoadAction> afterLoadActions,
      SharedSessionContractImplementor session) throws SQLException {
    mybatisQuery = queryParameters.getResultTransformer() instanceof MybatisResultTransformer;
    return super.executeQueryStatement(queryParameters, scroll, afterLoadActions, session);
  }

  @Override
  protected void autoDiscoverTypes(ResultSet rs) {
    if (!mybatisQuery) {
      super.autoDiscoverTypes(rs);
    }
  }

}
