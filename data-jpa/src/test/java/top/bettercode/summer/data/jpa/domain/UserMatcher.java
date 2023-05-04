package top.bettercode.summer.data.jpa.domain;

import top.bettercode.summer.data.jpa.query.PathMatcher;
import top.bettercode.summer.data.jpa.query.SpecMatcher;
import top.bettercode.summer.data.jpa.query.SpecMatcherMode;
import top.bettercode.summer.data.jpa.query.SpecPath;

import java.time.LocalDateTime;

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
  public SpecPath<User, UserMatcher> id() {
    return super.specPath("id");
  }

  /**
   * @param id 主键
   * @return 主键 相关Matcher
   */
  public UserMatcher id(Integer id) {
    super.specPath("id").setValue(id);
    return this;
  }

  /**
   * @param id 主键
   * @param matcher PathMatcher
   * @return 主键 相关Matcher
   */
  public UserMatcher id(Integer id, PathMatcher matcher) {
    super.specPath("id").setValue(id).withMatcher(matcher);
    return this;
  }

  /**
   * @return 用户名称 相关Matcher
   */
  public SpecPath<User, UserMatcher> firstName() {
    return super.specPath("firstName");
  }

  /**
   * @param firstName 用户名称
   * @return 用户名称 相关Matcher
   */
  public UserMatcher firstName(String firstName) {
    super.specPath("firstName").setValue(firstName);
    return this;
  }

  /**
   * @param firstName 用户名称
   * @param matcher PathMatcher
   * @return 用户名称 相关Matcher
   */
  public UserMatcher firstName(String firstName, PathMatcher matcher) {
    super.specPath("firstName").setValue(firstName).withMatcher(matcher);
    return this;
  }

  /**
   * @return 用户名称 相关Matcher
   */
  public SpecPath<User, UserMatcher> lastName() {
    return super.specPath("lastName");
  }

  /**
   * @param lastName 用户名称
   * @return 用户名称 相关Matcher
   */
  public UserMatcher lastName(String lastName) {
    super.specPath("lastName").setValue(lastName);
    return this;
  }

  /**
   * @param lastName 用户名称
   * @param matcher PathMatcher
   * @return 用户名称 相关Matcher
   */
  public UserMatcher lastName(String lastName, PathMatcher matcher) {
    super.specPath("lastName").setValue(lastName).withMatcher(matcher);
    return this;
  }

  /**
   * @return 版本号 相关Matcher
   */
  public SpecPath<User, UserMatcher> version() {
    return super.specPath("version");
  }

  /**
   * @param version 版本号 默认值：0
   * @return 版本号 相关Matcher
   */
  public UserMatcher version(Integer version) {
    super.specPath("version").setValue(version);
    return this;
  }

  /**
   * @param version 版本号 默认值：0
   * @param matcher PathMatcher
   * @return 版本号 相关Matcher
   */
  public UserMatcher version(Integer version, PathMatcher matcher) {
    super.specPath("version").setValue(version).withMatcher(matcher);
    return this;
  }

  /**
   * @return 逻辑删除 相关Matcher
   */
  public SpecPath<User, UserMatcher> deleted() {
    return super.specPath("deleted");
  }

  /**
   * @param deleted 逻辑删除
   * @return 逻辑删除 相关Matcher
   */
  public UserMatcher deleted(Boolean deleted) {
    super.specPath("deleted").setValue(deleted);
    return this;
  }

  /**
   * @param deleted 逻辑删除
   * @param matcher PathMatcher
   * @return 逻辑删除 相关Matcher
   */
  public UserMatcher deleted(Boolean deleted, PathMatcher matcher) {
    super.specPath("deleted").setValue(deleted).withMatcher(matcher);
    return this;
  }

  /**
   * @return 修改时间 相关Matcher
   */
  public SpecPath<User, UserMatcher> lastModifiedDate() {
    return super.specPath("lastModifiedDate");
  }

  /**
   * @param lastModifiedDate 修改时间 默认值：CURRENT_TIMESTAMP
   * @return 修改时间 相关Matcher
   */
  public UserMatcher lastModifiedDate(LocalDateTime lastModifiedDate) {
    super.specPath("lastModifiedDate").setValue(lastModifiedDate);
    return this;
  }

  /**
   * @param lastModifiedDate 修改时间 默认值：CURRENT_TIMESTAMP
   * @param matcher PathMatcher
   * @return 修改时间 相关Matcher
   */
  public UserMatcher lastModifiedDate(LocalDateTime lastModifiedDate, PathMatcher matcher) {
    super.specPath("lastModifiedDate").setValue(lastModifiedDate).withMatcher(matcher);
    return this;
  }
}