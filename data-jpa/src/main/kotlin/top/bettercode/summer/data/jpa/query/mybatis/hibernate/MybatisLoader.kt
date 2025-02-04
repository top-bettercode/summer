package top.bettercode.summer.data.jpa.query.mybatis.hibernate

import org.hibernate.engine.spi.QueryParameters
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.hql.internal.HolderInstantiator
import org.hibernate.loader.custom.CustomLoader
import org.hibernate.loader.custom.CustomQuery
import org.hibernate.loader.spi.AfterLoadAction
import org.hibernate.query.spi.ScrollableResultsImplementor
import org.hibernate.transform.ResultTransformer
import org.hibernate.type.Type
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultTransformer
import java.sql.ResultSet

/**
 * @author Peter Wu
 */
class MybatisLoader(customQuery: CustomQuery?, factory: SessionFactoryImplementor?) :
    CustomLoader(customQuery, factory) {
    private var mybatisQuery = false


    override fun scroll(
        queryParameters: QueryParameters?,
        returnTypes: Array<out Type>?,
        holderInstantiator: HolderInstantiator?,
        session: SharedSessionContractImplementor?
    ): ScrollableResultsImplementor {
        val resultTransformer = queryParameters?.resultTransformer
        if (resultTransformer is MybatisResultTransformer) {
            mybatisQuery = true
            resultTransformer.autoCloseResultSet = false
        }
        return super.scroll(
            queryParameters,
            returnTypes,
            if (mybatisQuery) HolderInstantiator.NOOP_INSTANTIATOR else holderInstantiator,
            session
        )
    }

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
    ): Any? {
        return if (mybatisQuery) {
            (queryParameters.resultTransformer as MybatisResultTransformer).transformResultSet(
                resultSet
            )
        } else {
            super.loadSingleRow(resultSet, session, queryParameters, returnProxies)
        }
    }

    override fun processResultSet(
        rs: ResultSet, queryParameters: QueryParameters,
        session: SharedSessionContractImplementor, returnProxies: Boolean,
        forcedResultTransformer: ResultTransformer?, maxRows: Int,
        afterLoadActions: List<AfterLoadAction>
    ): List<*> {
        return if (mybatisQuery) {
            (queryParameters.resultTransformer as MybatisResultTransformer).transformListResultSet(
                rs,
                maxRows
            )
        } else {
            super.processResultSet(
                rs, queryParameters, session, returnProxies,
                forcedResultTransformer, maxRows, afterLoadActions
            )
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
