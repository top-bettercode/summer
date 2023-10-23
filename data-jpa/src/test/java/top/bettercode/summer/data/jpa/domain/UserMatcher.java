package top.bettercode.summer.data.jpa.domain;

import java.time.LocalDateTime;
import top.bettercode.summer.data.jpa.query.PathMatcher;
import top.bettercode.summer.data.jpa.query.SpecMatcher;
import top.bettercode.summer.data.jpa.query.SpecMatcherMode;
import top.bettercode.summer.data.jpa.query.SpecPath;

/**
 * 后台用户 SpecMatcher
 */
public class UserMatcher extends SpecMatcher<User, UserMatcher> {

  private static final long serialVersionUID = 1L;

  private UserMatcher(SpecMatcherMode mode, User probe) {
    super(mode, probe);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 后台用户 SpecMatcher 实例
   */
  public static UserMatcher matching() {
    return new UserMatcher(SpecMatcherMode.ALL, null);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 后台用户 SpecMatcher 实例
   */
  public static UserMatcher matching(User probe) {
    return new UserMatcher(SpecMatcherMode.ALL, probe);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 后台用户 SpecMatcher 实例
   */
  public static UserMatcher matchingAny() {
    return new UserMatcher(SpecMatcherMode.ANY, null);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @return 后台用户 SpecMatcher 实例
   */
  public static UserMatcher matchingAny(User probe) {
    return new UserMatcher(SpecMatcherMode.ANY, probe);
  }

  /**
   * @return 主键 相关Matcher
   */
  public SpecPath<Integer, User, UserMatcher> id() {
    return super.path("id");
  }

  /**
   * @param id 主键
   * @return 主键 相关Matcher
   */
  public UserMatcher id(Integer id) {
    super.path("id").criteria(id);
    return this;
  }

  /**
   * @param id      主键
   * @param matcher PathMatcher
   * @return 主键 相关Matcher
   */
  public UserMatcher id(Integer id, PathMatcher matcher) {
    super.path("id").criteria(id).withMatcher(matcher);
    return this;
  }

  /**
   * @return 用户名称 相关Matcher
   */
  public SpecPath<String, User, UserMatcher> firstName() {
    return super.path("firstName");
  }

  /**
   * @param firstName 用户名称
   * @return 用户名称 相关Matcher
   */
  public UserMatcher firstName(String firstName) {
    super.path("firstName").criteria(firstName);
    return this;
  }

  public UserMatcher setFirstName(String firstName) {
    return super.criteriaUpdate("firstName", firstName);
  }

  public UserMatcher setLastName(String lastName) {
    return super.criteriaUpdate("lastName", lastName);
  }

  /**
   * @param firstName 用户名称
   * @param matcher   PathMatcher
   * @return 用户名称 相关Matcher
   */
  public UserMatcher firstName(String firstName, PathMatcher matcher) {
    super.path("firstName").criteria(firstName).withMatcher(matcher);
    return this;
  }

  /**
   * @return 用户名称 相关Matcher
   */
  public SpecPath<String, User, UserMatcher> lastName() {
    return super.path("lastName");
  }

  /**
   * @param lastName 用户名称
   * @return 用户名称 相关Matcher
   */
  public UserMatcher lastName(String lastName) {
    super.path("lastName").criteria(lastName);
    return this;
  }

  /**
   * @param lastName 用户名称
   * @param matcher  PathMatcher
   * @return 用户名称 相关Matcher
   */
  public UserMatcher lastName(String lastName, PathMatcher matcher) {
    super.path("lastName").criteria(lastName).withMatcher(matcher);
    return this;
  }

  /**
   * @return 版本号 相关Matcher
   */
  public SpecPath<Integer, User, UserMatcher> version() {
    return super.path("version");
  }

  /**
   * @param version 版本号 默认值：0
   * @return 版本号 相关Matcher
   */
  public UserMatcher version(Integer version) {
    super.path("version").criteria(version);
    return this;
  }

  /**
   * @param version 版本号 默认值：0
   * @param matcher PathMatcher
   * @return 版本号 相关Matcher
   */
  public UserMatcher version(Integer version, PathMatcher matcher) {
    super.path("version").criteria(version).withMatcher(matcher);
    return this;
  }

  /**
   * @return 逻辑删除 相关Matcher
   */
  public SpecPath<Boolean, User, UserMatcher> deleted() {
    return super.path("deleted");
  }

  /**
   * @param deleted 逻辑删除
   * @return 逻辑删除 相关Matcher
   */
  public UserMatcher deleted(Boolean deleted) {
    super.path("deleted").criteria(deleted);
    return this;
  }

  /**
   * @param deleted 逻辑删除
   * @param matcher PathMatcher
   * @return 逻辑删除 相关Matcher
   */
  public UserMatcher deleted(Boolean deleted, PathMatcher matcher) {
    super.path("deleted").criteria(deleted).withMatcher(matcher);
    return this;
  }

  /**
   * @return 修改时间 相关Matcher
   */
  public SpecPath<LocalDateTime, User, UserMatcher> lastModifiedDate() {
    return super.path("lastModifiedDate");
  }

  /**
   * @param lastModifiedDate 修改时间 默认值：CURRENT_TIMESTAMP
   * @return 修改时间 相关Matcher
   */
  public UserMatcher lastModifiedDate(LocalDateTime lastModifiedDate) {
    super.path("lastModifiedDate").criteria(lastModifiedDate);
    return this;
  }

  /**
   * @param lastModifiedDate 修改时间 默认值：CURRENT_TIMESTAMP
   * @param matcher          PathMatcher
   * @return 修改时间 相关Matcher
   */
  public UserMatcher lastModifiedDate(LocalDateTime lastModifiedDate, PathMatcher matcher) {
    super.path("lastModifiedDate").criteria(lastModifiedDate).withMatcher(matcher);
    return this;
  }
}
