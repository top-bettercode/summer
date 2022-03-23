package org.springframework.data.jpa.repository.query.mybatis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import org.apache.ibatis.mapping.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.jpa.support.JpaUtil;

public class TuplesWrapper {

  private final Logger log = LoggerFactory.getLogger(TuplesWrapper.class);

  private int index = 0;
  private Tuple tuple;
  private final List<Tuple> tuples;
  private final List<String> columnNames = new ArrayList<>();
  private final List<String> classNames = new ArrayList<>();
  private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
  private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

  public TuplesWrapper(List<Tuple> tuples) {
    Assert.isTrue(tuples.size() > 0, "tuples must not be empty");
    this.tuples = tuples;
    List<TupleElement<?>> elements = tuples.get(0).getElements();
    for (TupleElement<?> element : elements) {
      String alias = element.getAlias();
      columnNames.add(alias);
      Class<?> javaType = element.getJavaType();
      classNames.add(javaType.getName());
    }
  }

  public int size() {
    return tuples.size();
  }

  public boolean next() {
    if (index < tuples.size()) {
      tuple = tuples.get(index++);
      return true;
    } else {
      return false;
    }
  }

  public Object get(String alias) {
    return tuple.get(alias);
  }

  @SuppressWarnings("unchecked")
  public <X> X get(String alias, Class<X> type) {
    Object source = this.get(alias);
    try {
      return JpaUtil.convert(source, type);
    } catch (Exception e) {
      if (String.class.equals(type)) {
        log.warn(alias
            + "automatic conversion String failed，use toString() enforce cast, please consider receiving with the correct type");
        return (X) source.toString();
      }
      throw new RuntimeException(alias + " : " + e.getMessage(), e);
    }
  }

  public List<String> getColumnNames() {
    return this.columnNames;
  }

  public List<String> getClassNames() {
    return Collections.unmodifiableList(classNames);
  }

  private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
    List<String> mappedColumnNames = new ArrayList<>();
    List<String> unmappedColumnNames = new ArrayList<>();
    final String upperColumnPrefix =
        columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
    final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(),
        upperColumnPrefix);
    for (String columnName : columnNames) {
      final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
      if (mappedColumns.contains(upperColumnName)) {
        mappedColumnNames.add(upperColumnName);
      } else {
        unmappedColumnNames.add(columnName);
      }
    }
    mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
    unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
  }

  public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) {
    List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    if (mappedColumnNames == null) {
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return mappedColumnNames;
  }

  public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) {
    List<String> unMappedColumnNames = unMappedColumnNamesMap.get(
        getMapKey(resultMap, columnPrefix));
    if (unMappedColumnNames == null) {
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return unMappedColumnNames;
  }

  private String getMapKey(ResultMap resultMap, String columnPrefix) {
    return resultMap.getId() + ":" + columnPrefix;
  }

  private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
    if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
      return columnNames;
    }
    final Set<String> prefixed = new HashSet<>();
    for (String columnName : columnNames) {
      prefixed.add(prefix + columnName);
    }
    return prefixed;
  }

}
