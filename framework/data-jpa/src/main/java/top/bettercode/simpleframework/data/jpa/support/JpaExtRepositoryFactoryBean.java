package top.bettercode.simpleframework.data.jpa.support;

import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.config.JpaExtProperties;
import java.io.Serializable;
import javax.persistence.EntityManager;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.repository.support.JpaExtRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

/**
 * {@link JpaRepositoryFactoryBean} to return a custom repository base class.
 *
 * @author Peter Wu
 */
public class JpaExtRepositoryFactoryBean<T extends JpaExtRepository<Object, Serializable>>
    extends JpaRepositoryFactoryBean<T, Object, Serializable> {

  private JpaExtProperties jpaExtProperties;
  private SqlSessionTemplate sqlSessionTemplate;

  public JpaExtRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  public void setJpaExtProperties(
      JpaExtProperties jpaExtProperties) {
    this.jpaExtProperties = jpaExtProperties;
  }

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.data.jpa.repository.support.
   * GenericJpaRepositoryFactoryBean#getFactory()
   */
  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
    Assert.notNull(sqlSessionTemplate, "sqlSessionTemplate must not be null");
    Assert.notNull(jpaExtProperties, "jpaExtProperties must not be null");
    return new JpaExtRepositoryFactory(em, sqlSessionTemplate, jpaExtProperties);
  }

}
