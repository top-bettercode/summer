package org.springframework.data.jpa.repository.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.springframework.data.jpa.repository.query.mybatis.MybatisParam;

/**
 * @author Peter Wu
 */
@SuppressWarnings("rawtypes")
public class MybatisQuery implements Query {

  private final String queryString;
  private final Query query;
  private final MybatisParam mybatisParam;

  public MybatisQuery(String queryString,
      Query query,
      MybatisParam mybatisParam) {
    this.queryString = queryString;
    this.query = query;
    this.mybatisParam = mybatisParam;
  }

  public String getQueryString() {
    return queryString;
  }

  public Query getQuery() {
    return query;
  }

  public MybatisParam getMybatisParam() {
    return mybatisParam;
  }

  @Override
  public List getResultList() {
    return query.getResultList();
  }

  @Override
  public Stream getResultStream() {
    return query.getResultStream();
  }

  @Override
  public Object getSingleResult() {
    return query.getSingleResult();
  }

  @Override
  public int executeUpdate() {
    return query.executeUpdate();
  }

  @Override
  public Query setMaxResults(int maxResult) {
    return query.setMaxResults(maxResult);
  }

  @Override
  public int getMaxResults() {
    return query.getMaxResults();
  }

  @Override
  public Query setFirstResult(int startPosition) {
    return query.setFirstResult(startPosition);
  }

  @Override
  public int getFirstResult() {
    return query.getFirstResult();
  }

  @Override
  public Query setHint(String hintName, Object value) {
    return query.setHint(hintName, value);
  }

  @Override
  public Map<String, Object> getHints() {
    return query.getHints();
  }

  @Override
  public <T> Query setParameter(Parameter<T> param, T value) {
    return query.setParameter(param, value);
  }

  @Override
  public Query setParameter(Parameter<Calendar> param,
      Calendar value, TemporalType temporalType) {
    return query.setParameter(param, value, temporalType);
  }

  @Override
  public Query setParameter(Parameter<Date> param, Date value,
      TemporalType temporalType) {
    return query.setParameter(param, value, temporalType);
  }

  @Override
  public Query setParameter(String name, Object value) {
    return query.setParameter(name, value);
  }

  @Override
  public Query setParameter(String name, Calendar value,
      TemporalType temporalType) {
    return query.setParameter(name, value, temporalType);
  }

  @Override
  public Query setParameter(String name, Date value,
      TemporalType temporalType) {
    return query.setParameter(name, value, temporalType);
  }

  @Override
  public Query setParameter(int position, Object value) {
    return query.setParameter(position, value);
  }

  @Override
  public Query setParameter(int position, Calendar value,
      TemporalType temporalType) {
    return query.setParameter(position, value, temporalType);
  }

  @Override
  public Query setParameter(int position, Date value,
      TemporalType temporalType) {
    return query.setParameter(position, value, temporalType);
  }

  @Override
  public Set<Parameter<?>> getParameters() {
    return query.getParameters();
  }

  @Override
  public Parameter<?> getParameter(String name) {
    return query.getParameter(name);
  }

  @Override
  public <T> Parameter<T> getParameter(String name, Class<T> type) {
    return query.getParameter(name, type);
  }

  @Override
  public Parameter<?> getParameter(int position) {
    return query.getParameter(position);
  }

  @Override
  public <T> Parameter<T> getParameter(int position, Class<T> type) {
    return query.getParameter(position, type);
  }

  @Override
  public boolean isBound(Parameter<?> param) {
    return query.isBound(param);
  }

  @Override
  public <T> T getParameterValue(Parameter<T> param) {
    return query.getParameterValue(param);
  }

  @Override
  public Object getParameterValue(String name) {
    return query.getParameterValue(name);
  }

  @Override
  public Object getParameterValue(int position) {
    return query.getParameterValue(position);
  }

  @Override
  public Query setFlushMode(FlushModeType flushMode) {
    return query.setFlushMode(flushMode);
  }

  @Override
  public FlushModeType getFlushMode() {
    return query.getFlushMode();
  }

  @Override
  public Query setLockMode(LockModeType lockMode) {
    return query.setLockMode(lockMode);
  }

  @Override
  public LockModeType getLockMode() {
    return query.getLockMode();
  }

  @Override
  public <T> T unwrap(Class<T> cls) {
    return query.unwrap(cls);
  }
}
