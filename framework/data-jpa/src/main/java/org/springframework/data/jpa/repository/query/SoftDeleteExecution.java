package org.springframework.data.jpa.repository.query;

import cn.bestwu.simpleframework.data.jpa.support.SoftDeleteSupport;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * {@link JpaQueryExecution} removing entities matching the query.
 *
 * @author Peter Wu
 */
public class SoftDeleteExecution extends JpaQueryExecution {

  private final EntityManager em;
  private final SoftDeleteSupport softDeleteSupport;

  public SoftDeleteExecution(EntityManager em,
      SoftDeleteSupport softDeleteSupport) {
    this.em = em;
    this.softDeleteSupport = softDeleteSupport;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.jpa.repository.query.JpaQueryExecution#doExecute(org.springframework.data.jpa.repository.query.AbstractJpaQuery, java.lang.Object[])
   */
  @Override
  protected Object doExecute(AbstractJpaQuery jpaQuery, Object[] values) {

    Query query = jpaQuery.createQuery(values);
    List<?> resultList = query.getResultList();

    if (softDeleteSupport.support()) {
      for (Object o : resultList) {
        softDeleteSupport.setSoftDeleted(o);
        em.merge(o);
      }
    } else {
      for (Object o : resultList) {
        em.remove(o);
      }
    }

    return jpaQuery.getQueryMethod().isCollectionQuery() ? resultList : resultList.size();
  }
}
