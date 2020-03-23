package cn.bestwu.simpleframework.data.binding;

import com.baomidou.mybatisplus.enums.SqlLike;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 有条件的Wrapper
 *
 * @author Peter Wu
 */
public class ConditionWrapper<T> extends EntityWrapper<T> {

  private static final long serialVersionUID = 1L;
  private final Map<String, String> conditions = new HashMap<>();
  private final Map<String, String> additions = new HashMap<>();
  private boolean setOrderBy = false;
  private boolean setGroupBy = false;
  private boolean setIsNull = false;
  private boolean setIsNotNull = false;
  private boolean trim = false;
  //--------------------------------------------

  //--------------------------------------------

  public void doDefault() {
    new HashMap<>(conditions).forEach(this::eq);
  }

  public void addConditions(String column, String value) {
    conditions.put(column, value);
  }

  public void setTrim(boolean trim) {
    this.trim = trim;
  }

  public String getConditionValue(String column) {
    String value = conditions.get(column);
    return trim && value != null ? value.trim() : value;
  }

  public void addAdditions(String property, String value) {
    additions.put(property, value);
  }

  public String getAdditionValue(String property) {
    return additions.get(property);
  }

  public boolean isSetOrderBy() {
    return setOrderBy;
  }

  public void setSetOrderBy(boolean setOrderBy) {
    this.setOrderBy = setOrderBy;
  }

  public boolean isSetGroupBy() {
    return setGroupBy;
  }

  public void setSetGroupBy(boolean setGroupBy) {
    this.setGroupBy = setGroupBy;
  }

  public boolean isSetIsNull() {
    return setIsNull;
  }

  public void setSetIsNull(boolean setIsNull) {
    this.setIsNull = setIsNull;
  }

  public boolean isSetIsNotNull() {
    return setIsNotNull;
  }

  public void setSetIsNotNull(boolean setIsNotNull) {
    this.setIsNotNull = setIsNotNull;
  }
  //--------------------------------------------

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> eq(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.eq(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> eq(String column, Object params) {
    conditions.remove(column);
    return super.eq(column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> ne(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.ne(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> ne(String column, Object params) {
    conditions.remove(column);
    return super.ne(column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> gt(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.gt(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> gt(String column, Object params) {
    conditions.remove(column);
    return super.gt(column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> ge(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.ge(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> ge(String column, Object params) {
    conditions.remove(column);
    return super.ge(column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> lt(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.lt(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> lt(String column, Object params) {
    conditions.remove(column);
    return super.lt(column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   */
  @Override
  public Wrapper<T> le(boolean condition, String column, Object params) {
    conditions.remove(column);
    return super.le(condition, column, params);
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   */
  @Override
  public Wrapper<T> le(String column, Object params) {
    conditions.remove(column);
    return super.le(column, params);
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> like(boolean condition, String column, String value) {
    conditions.remove(column);
    return super.like(condition, column, value);
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> like(String column, String value) {
    conditions.remove(column);
    return super.like(column, value);
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> notLike(boolean condition, String column, String value) {
    conditions.remove(column);
    return super.notLike(condition, column, value);
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> notLike(String column, String value) {
    conditions.remove(column);
    return super.notLike(column, value);
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> like(boolean condition, String column, String value, SqlLike type) {
    conditions.remove(column);
    return super.like(condition, column, value, type);
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> like(String column, String value, SqlLike type) {
    conditions.remove(column);
    return super.like(column, value, type);
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> notLike(boolean condition, String column, String value, SqlLike type) {
    conditions.remove(column);
    return super.notLike(condition, column, value, type);
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值
   * @return this
   */
  @Override
  public Wrapper<T> notLike(String column, String value, SqlLike type) {
    conditions.remove(column);
    return super.notLike(column, value, type);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 逗号拼接的字符串
   * @return this
   */
  @Override
  public Wrapper<T> in(boolean condition, String column, String value) {
    conditions.remove(column);
    return super.in(condition, column, value);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param column 字段名称
   * @param value 逗号拼接的字符串
   * @return this
   */
  @Override
  public Wrapper<T> in(String column, String value) {
    conditions.remove(column);
    return super.in(column, value);
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 逗号拼接的字符串
   * @return this
   */
  @Override
  public Wrapper<T> notIn(boolean condition, String column, String value) {
    conditions.remove(column);
    return super.notIn(condition, column, value);
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @param column 字段名称
   * @param value 逗号拼接的字符串
   * @return this
   */
  @Override
  public Wrapper<T> notIn(String column, String value) {
    conditions.remove(column);
    return super.notIn(column, value);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值 集合
   * @return this
   */
  @Override
  public Wrapper<T> in(boolean condition, String column, Collection<?> value) {
    conditions.remove(column);
    return super.in(condition, column, value);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值 集合
   * @return this
   */
  @Override
  public Wrapper<T> in(String column, Collection<?> value) {
    conditions.remove(column);
    return super.in(column, value);
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值 集合
   * @return this
   */
  @Override
  public Wrapper<T> notIn(boolean condition, String column, Collection<?> value) {
    conditions.remove(column);
    return super.notIn(condition, column, value);
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值 集合
   * @return this
   */
  @Override
  public Wrapper<T> notIn(String column, Collection<?> value) {
    conditions.remove(column);
    return super.notIn(column, value);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值 object数组
   * @return this
   */
  @Override
  public Wrapper<T> in(boolean condition, String column, Object[] value) {
    conditions.remove(column);
    return super.in(condition, column, value);
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值 object数组
   * @return this
   */
  @Override
  public Wrapper<T> in(String column, Object[] value) {
    conditions.remove(column);
    return super.in(column, value);
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @param value 匹配值 object数组
   * @return this
   */
  @Override
  public Wrapper<T> notIn(boolean condition, String column, Object... value) {
    conditions.remove(column);
    return super.notIn(condition, column, value);
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param column 字段名称
   * @param value 匹配值 object数组
   * @return this
   */
  @Override
  public Wrapper<T> notIn(String column, Object... value) {
    conditions.remove(column);
    return super.notIn(column, value);
  }

  /**
   * <p>
   * betwwee 条件语句
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @return this
   */
  @Override
  public Wrapper<T> between(boolean condition, String column, Object val1, Object val2) {
    conditions.remove(column);
    return super.between(condition, column, val1, val2);
  }

  /**
   * <p>
   * betwwee 条件语句
   * </p>
   *
   * @param column 字段名称
   * @return this
   */
  @Override
  public Wrapper<T> between(String column, Object val1, Object val2) {
    conditions.remove(column);
    return super.between(column, val1, val2);
  }

  /**
   * <p>
   * NOT betwwee 条件语句
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param column 字段名称
   * @return this
   */
  @Override
  public Wrapper<T> notBetween(boolean condition, String column, Object val1, Object val2) {
    conditions.remove(column);
    return super.notBetween(condition, column, val1, val2);
  }

  /**
   * <p>
   * NOT betwwee 条件语句
   * </p>
   *
   * @param column 字段名称
   * @return this
   */
  @Override
  public Wrapper<T> notBetween(String column, Object val1, Object val2) {
    conditions.remove(column);
    return super.notBetween(column, val1, val2);
  }

}
