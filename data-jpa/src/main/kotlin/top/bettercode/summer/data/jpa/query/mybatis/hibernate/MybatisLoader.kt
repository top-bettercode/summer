package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.HibernateException
import org.hibernate.QueryException
import org.hibernate.engine.spi.QueryParameters
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.loader.custom.CustomLoader
import org.hibernate.loader.custom.CustomQuery
import org.hibernate.loader.spi.AfterLoadAction
import org.hibernate.transform.ResultTransformer
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultTransformer
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Peter Wu
 */
class MybatisLoader(customQuery: CustomQuery?, factory: SessionFactoryImplementor?) : CustomLoader(customQuery, factory) {
    private var mybatisQuery = false
    override fun getResultList(results: List<*>, resultTransformer: ResultTransformer?): List<*> {
        return if (mybatisQuery) {
            results
        } else {
            super.getResultList(results, resultTransformer)
        }
    }

    override fun loadSingleRow(
            resultSet: ResultSet, session: SharedSessionContractImplementor,
            queryParameters: QueryParameters, returnProxies: Boolean
    ): Any {
        return if (mybatisQuery) {
            (queryParameters.resultTransformer as MybatisResultTransformer).transformResultSet(
                    resultSet)
        } else {
            super.loadSingleRow(resultSet, session, queryParameters, returnProxies)
        }
    }

    override fun processResultSet(
            rs: ResultSet, queryParameters: QueryParameters,
            session: SharedSessionContractImplementor, returnProxies: Boolean,
            forcedResultTransformer: ResultTransformer?, maxRows: Int,
            afterLoadActions: List<AfterLoadAction>
    ): List<*>? {
        return if (mybatisQuery) {
            (queryParameters.resultTransformer as MybatisResultTransformer).transformListResultSet(rs,
                    maxRows)
        } else {
            super.processResultSet(rs, queryParameters, session, returnProxies,
                    forcedResultTransformer, maxRows, afterLoadActions)
        }
    }

    override fun executeQueryStatement(
            queryParameters: QueryParameters,
            scroll: Boolean, afterLoadActions: List<AfterLoadAction>,
            session: SharedSessionContractImplementor
    ): SqlStatementWrapper {
        mybatisQuery = queryParameters.resultTransformer is MybatisResultTransformer
        return super.executeQueryStatement(queryParameters, scroll, afterLoadActions, session)
    }

    override fun autoDiscoverTypes(rs: ResultSet) {
        if (!mybatisQuery) {
            super.autoDiscoverTypes(rs)
        }
    }
}
