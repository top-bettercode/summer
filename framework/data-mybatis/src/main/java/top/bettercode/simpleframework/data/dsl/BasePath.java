package top.bettercode.simpleframework.data.dsl;

import com.baomidou.mybatisplus.enums.SqlLike;
import java.io.Serializable;
import java.util.Collection;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class BasePath<E extends EntityPathWrapper<E, T>, T> implements Serializable {

  private static final long serialVersionUID = 1621419386323928785L;
  private final E wrapper;
  private final String column;

  public BasePath(E wrapper, String column) {
    this.wrapper = wrapper;
    this.column = column;
  }

  //--------------------------------------------

  public E trim(boolean trim) {
    wrapper.setTrim(trim);
    return wrapper;
  }

  //--------------------------------------------

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   *
   * @param params params
   * @param condition 拼接的前置条件
   * @return E
   */
  public E eq(boolean condition, Object params) {
    wrapper.eq(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E eq(boolean condition) {
    wrapper.eq(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E eq(Object params) {
    wrapper.eq(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field=value"表达式
   * </p>
   *
   * @return E
   */
  public E eq() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.eq(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   *
   * @param params params
   * @param condition 拼接的前置条件
   * @return E
   */
  public E ne(boolean condition, Object params) {
    wrapper.ne(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E ne(boolean condition) {
    wrapper.ne(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E ne(Object params) {
    wrapper.ne(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field &lt;&gt; value"表达式
   * </p>
   *
   * @return E
   */
  public E ne() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.ne(StringUtils.hasText(conditionValue), column,
        conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param params params
   * @return E
   */
  public E gt(boolean condition, Object params) {
    wrapper.gt(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E gt(boolean condition) {
    wrapper.gt(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E gt(Object params) {
    wrapper.gt(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;value"表达式
   * </p>
   *
   * @return E
   */
  public E gt() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.gt(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param params params
   * @return E
   */
  public E ge(boolean condition, Object params) {
    wrapper.ge(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E ge(boolean condition) {
    wrapper.ge(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E ge(Object params) {
    wrapper.ge(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&gt;=value"表达式
   * </p>
   *
   * @return E
   */
  public E ge() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.ge(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param params params
   * @return E
   */
  public E lt(boolean condition, Object params) {
    wrapper.lt(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E lt(boolean condition) {
    wrapper.lt(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E lt(Object params) {
    wrapper.lt(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;value"表达式
   * </p>
   *
   * @return E
   */
  public E lt() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.lt(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   *
   * @param params params
   * @param condition 拼接的前置条件
   * @return E
   */
  public E le(boolean condition, Object params) {
    wrapper.le(condition, column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E le(boolean condition) {
    wrapper.le(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   *
   * @param params params
   * @return E
   */
  public E le(Object params) {
    wrapper.le(column, params);
    return wrapper;
  }

  /**
   * <p>
   * 等同于SQL的"field&lt;=value"表达式
   * </p>
   *
   * @return E
   */
  public E le() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.le(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值
   * @return wrapper;
   */
  public E like(boolean condition, String value) {
    wrapper.like(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E like(boolean condition) {
    wrapper.like(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param value 匹配值
   * @return wrapper;
   */
  public E like(String value) {
    wrapper.like(column, value);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @return wrapper;
   */
  public E like() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.like(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值
   * @return wrapper;
   */
  public E notLike(boolean condition, String value) {
    wrapper.notLike(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E notLike(boolean condition) {
    wrapper.notLike(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param value 匹配值
   * @return wrapper;
   */
  public E notLike(String value) {
    wrapper.notLike(column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @return wrapper;
   */
  public E notLike() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.notLike(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param condition 拼接的前置条件
   * @param value 匹配值
   * @return wrapper;
   */
  public E like(boolean condition, String value, SqlLike type) {
    wrapper.like(condition, column, value, type);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E like(boolean condition, SqlLike type) {
    wrapper.like(condition, column, wrapper.getConditionValue(column), type);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param value 匹配值
   * @return wrapper;
   */
  public E like(String value, SqlLike type) {
    wrapper.like(column, value, type);
    return wrapper;
  }

  /**
   * <p>
   * LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @return wrapper;
   */
  public E like(SqlLike type) {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.like(StringUtils.hasText(conditionValue), column, conditionValue, type);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param condition 拼接的前置条件
   * @param value 匹配值
   * @return wrapper;
   */
  public E notLike(boolean condition, String value, SqlLike type) {
    wrapper.notLike(condition, column, value, type);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E notLike(boolean condition, SqlLike type) {
    wrapper.notLike(condition, column, wrapper.getConditionValue(column), type);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @param value 匹配值
   * @return wrapper;
   */
  public E notLike(String value, SqlLike type) {
    wrapper.notLike(column, value, type);
    return wrapper;
  }

  /**
   * <p>
   * NOT LIKE条件语句，value中无需前后%
   * </p>
   *
   * @param type type
   * @return wrapper;
   */
  public E notLike(SqlLike type) {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.notLike(StringUtils.hasText(conditionValue), column, conditionValue, type);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 逗号拼接的字符串
   * @return wrapper;
   */
  public E in(boolean condition, String value) {
    wrapper.in(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E in(boolean condition) {
    wrapper.in(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param value 逗号拼接的字符串
   * @return wrapper;
   */
  public E in(String value) {
    wrapper.in(column, value);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @return wrapper;
   */
  public E in() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.in(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 逗号拼接的字符串
   * @return wrapper;
   */
  public E notIn(boolean condition, String value) {
    wrapper.notIn(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E notIn(boolean condition) {
    wrapper.notIn(condition, column, wrapper.getConditionValue(column));
    return wrapper;
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @param value 逗号拼接的字符串
   * @return wrapper;
   */
  public E notIn(String value) {
    wrapper.notIn(column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN条件语句
   * </p>
   *
   * @return wrapper;
   */
  public E notIn() {
    String conditionValue = wrapper.getConditionValue(column);
    wrapper.notIn(StringUtils.hasText(conditionValue), column, conditionValue);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值 集合
   * @return wrapper;
   */
  public E in(boolean condition, Collection<?> value) {
    wrapper.in(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param value 匹配值 集合
   * @return wrapper;
   */
  public E in(Collection<?> value) {
    wrapper.in(column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值 集合
   * @return wrapper;
   */
  public E notIn(boolean condition, Collection<?> value) {
    wrapper.notIn(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param value 匹配值 集合
   * @return wrapper;
   */
  public E notIn(Collection<?> value) {
    wrapper.notIn(column, value);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值 object数组
   * @return wrapper;
   */
  public E in(boolean condition, Object... value) {
    wrapper.in(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param value 匹配值 object数组
   * @return wrapper;
   */
  public E in(Object... value) {
    wrapper.in(column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param value 匹配值 object数组
   * @return wrapper;
   */
  public E notIn(boolean condition, Object... value) {
    wrapper.notIn(condition, column, value);
    return wrapper;
  }

  /**
   * <p>
   * NOT IN 条件语句，目前适配mysql及oracle
   * </p>
   *
   * @param value 匹配值 object数组
   * @return wrapper;
   */
  public E notIn(Object... value) {
    wrapper.notIn(column, value);
    return wrapper;
  }

  /**
   * <p>
   * betwwee 条件语句
   * </p>
   *
   * @param val1 val1
   * @param val2 val2
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E between(boolean condition, Object val1, Object val2) {
    wrapper.between(condition, column, val1, val2);
    return wrapper;
  }

  /**
   * <p>
   * betwwee 条件语句
   * </p>
   *
   * @param val1 val1
   * @param val2 val2
   * @return wrapper;
   */
  public E between(Object val1, Object val2) {
    wrapper.between(column, val1, val2);
    return wrapper;
  }

  /**
   * <p>
   * NOT betwwee 条件语句
   * </p>
   *
   * @param val1 val1
   * @param val2 val2
   * @param condition 拼接的前置条件
   * @return wrapper;
   */
  public E notBetween(boolean condition, Object val1, Object val2) {
    wrapper.notBetween(condition, column, val1, val2);
    return wrapper;
  }

  /**
   * <p>
   * NOT betwwee 条件语句
   * </p>
   *
   * @param val1 val1
   * @param val2 val2
   * @return wrapper;
   */
  public E notBetween(Object val1, Object val2) {
    wrapper.notBetween(column, val1, val2);
    return wrapper;
  }

  /**
   * <p>
   * is not null 条件
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E isNotNull(boolean condition) {
    wrapper.isNotNull(condition, column);
    return wrapper;
  }

  /**
   * <p>
   * is not null 条件
   * </p>
   *
   * @return E
   */
  public E isNotNull() {
    wrapper.isNotNull(column);
    return wrapper;
  }

  /**
   * <p>
   * is null 条件
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E isNull(boolean condition) {
    wrapper.isNull(condition, column);
    return wrapper;
  }

  /**
   * <p>
   * is null 条件
   * </p>
   *
   * @return E
   */
  public E isNull() {
    wrapper.isNull(column);
    return wrapper;
  }

  /**
   * <p>
   * SQL中orderby关键字跟的条件语句
   * </p>
   * <p>
   * eg: ew.groupBy("id,name").having("id={0}",22).and("password is not null"
   * ).orderBy("id,name")
   * </p>
   *
   * @param condition 拼接的前置条件
   * @return E
   */
  public E orderBy(boolean condition) {
    wrapper.orderBy(condition, column);
    return wrapper;
  }

  /**
   * <p>
   * SQL中orderby关键字跟的条件语句
   * </p>
   * <p>
   * eg: ew.groupBy("id,name").having("id={0}",22).and("password is not null"
   * ).orderBy("id,name")
   * </p>
   *
   * @return E
   */
  public E orderBy() {
    wrapper.orderBy(column);
    return wrapper;
  }

  /**
   * <p>
   * SQL中orderby关键字跟的条件语句，可根据变更动态排序
   * </p>
   *
   * @param condition 拼接的前置条件
   * @param isAsc 是否为升序
   * @return E
   */
  public E orderBy(boolean condition, boolean isAsc) {
    wrapper.orderBy(condition, column, isAsc);
    return wrapper;
  }


  /**
   * <p>
   * 批量根据ASC排序
   * </p>
   *
   * @return E
   */
  public E orderAsc() {
    wrapper.orderBy(true, column, true);
    return wrapper;
  }

  /**
   * <p>
   * 批量根据DESC排序
   * </p>
   *
   * @return E
   */
  public E orderDesc() {
    wrapper.orderBy(true, column, false);
    return wrapper;
  }

}
