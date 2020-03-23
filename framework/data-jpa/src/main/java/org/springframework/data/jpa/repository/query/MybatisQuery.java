package org.springframework.data.jpa.repository.query;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.repository.query.mybatis.JpaExtQueryMethod;

public class MybatisQuery extends AbstractJpaQuery {

	private final SqlSessionTemplate sqlSessionTemplate;

	public MybatisQuery(JpaExtQueryMethod method, EntityManager em,
			SqlSessionTemplate sqlSessionTemplate) {
		super(method, em);
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	@Override
	public JpaExtQueryMethod getQueryMethod() {
		return (JpaExtQueryMethod) super.getQueryMethod();
	}

	@Override
	protected JpaQueryExecution getExecution() {
		JpaQueryMethod queryMethod = getQueryMethod();
		if (queryMethod.isStreamQuery()) {
			return new MybatisQueryExecution.StreamExecution();
		}
		else if (queryMethod.isCollectionQuery()) {
			return new MybatisQueryExecution.CollectionExecution();
		}
		else if (queryMethod.isSliceQuery()) {
			return new MybatisQueryExecution.SlicedExecution();
		}
		else if (queryMethod.isPageQuery()) {
			return new MybatisQueryExecution.PagedExecution();
		}
		else if (queryMethod.isModifyingQuery()) {
			return new MybatisQueryExecution.ModifyingExecution();
		}
		else {
			return new MybatisQueryExecution.SingleEntityExecution();
		}
	}

	@Override
	protected Query doCreateQuery(Object[] objects) {
		return null;
	}

	@Override
	protected Query doCreateCountQuery(Object[] objects) {
		return null;
	}

	public SqlSessionTemplate getSqlSessionTemplate() {
		return sqlSessionTemplate;
	}
}
