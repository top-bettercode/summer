package top.bettercode.simpleframework.data.jpa.query.mybatis;

import org.hibernate.engine.query.spi.NativeQueryInterpreter;
import org.hibernate.engine.query.spi.NativeSQLQueryPlan;
import org.hibernate.engine.query.spi.ParamLocationRecognizer;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.custom.CustomLoader;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.loader.custom.sql.SQLCustomQuery;
import org.hibernate.query.internal.ParameterMetadataImpl;

public class MybatisQueryInterpreterStandardImpl implements NativeQueryInterpreter {

  private static final long serialVersionUID = 1L;
  private final SessionFactoryImplementor sessionFactory;

  public MybatisQueryInterpreterStandardImpl(SessionFactoryImplementor sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public ParameterMetadataImpl getParameterMetadata(String nativeQuery) {
    final ParamLocationRecognizer recognizer = ParamLocationRecognizer.parseLocations(nativeQuery,
        sessionFactory);
    return new ParameterMetadataImpl(
        recognizer.getOrdinalParameterDescriptionMap(),
        recognizer.getNamedParameterDescriptionMap()
    );
  }

  @Override
  public NativeSQLQueryPlan createQueryPlan(
      NativeSQLQuerySpecification specification,
      SessionFactoryImplementor sessionFactory) {
    CustomQuery customQuery = new SQLCustomQuery(
        specification.getQueryString(),
        specification.getQueryReturns(),
        specification.getQuerySpaces(),
        sessionFactory
    );

    return new NativeSQLQueryPlan(specification.getQueryString(), customQuery);
  }

  @SuppressWarnings("deprecation")
  @Override
  public CustomLoader createCustomLoader(CustomQuery customQuery,
      SessionFactoryImplementor sessionFactory) {
    return new MybatisLoader(customQuery, sessionFactory);
  }
}
