package top.bettercode.summer.data.jpa.query.mybatis;

import org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.custom.CustomLoader;
import org.hibernate.loader.custom.CustomQuery;

public class MybatisQueryInterpreterStandardImpl extends NativeQueryInterpreterStandardImpl {

  private static final long serialVersionUID = 1L;

  public MybatisQueryInterpreterStandardImpl(SessionFactoryImplementor sessionFactory) {
    super(sessionFactory);
  }

  @SuppressWarnings("deprecation")
  @Override
  public CustomLoader createCustomLoader(CustomQuery customQuery,
      SessionFactoryImplementor sessionFactory) {
    return new MybatisLoader(customQuery, sessionFactory);
  }
}
