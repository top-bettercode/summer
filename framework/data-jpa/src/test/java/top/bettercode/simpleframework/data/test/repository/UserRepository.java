package top.bettercode.simpleframework.data.test.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.mybatis.MybatisTemplate;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.jpa.querydsl.RecycleQuerydslPredicateExecutor;
import top.bettercode.simpleframework.data.jpa.support.Size;
import top.bettercode.simpleframework.data.test.domain.User;
import top.bettercode.simpleframework.data.test.resp.AUser;
import top.bettercode.simpleframework.data.test.resp.CUser;
import top.bettercode.simpleframework.data.test.resp.CUsers;

public interface UserRepository extends JpaExtRepository<User, Integer>,
    QuerydslPredicateExecutor<User>,
    RecycleQuerydslPredicateExecutor<User> {

  List<User> findByLastName(String lastName);

  Page<User> findByFirstName(String lastName, Pageable pageable);

  @Query(value = "select * from user where first_name = ?1", nativeQuery = true)
  Page<User> selectNativeSql(String first_name, Pageable pageable);


  @Modifying
  @Transactional
  void deleteByLastName(String lastName);

  //--------------------------------------------

  @MybatisTemplate
  List<CUser> selectResultMap(User user);

  @MybatisTemplate
  List<CUsers> selectResultMap2(User user);

//  Page<CUsers> selectResultMap2(User user, Pageable pageable);

  List<AUser> selectResultMap3(User user);

  Page<AUser> selectResultMap3(User user, Pageable pageable);

  @MybatisTemplate
  List<User> selectMybatisAll();

  @MybatisTemplate
  Page<User> selectMybatisAll(Pageable pageable);

  @MybatisTemplate
  @Select("select * from user where deleted = 0")
  List<User> selectByMybatisSize(Size size);

  List<User> selectMybatisIfParam(String firstName, String lastName);

  Stream<User> selectMybatisStream(String firstName, String lastName);

  @MybatisTemplate
  List<User> selectByMybatisMap(Map<String, String> param);

  @MybatisTemplate
  Page<User> selectByMybatisMap(Pageable pageable, Map<String, String> param);

  @MybatisTemplate
  List<User> selectByMybatisEntity(User user, Pageable pageable);

  @MybatisTemplate
  List<User> selectByMybatisSort(String firstName, Sort sort);

  @MybatisTemplate
  Page<User> selectByMybatisSort(String firstName, Pageable pageable);

  @MybatisTemplate
  User selectOneByMybatis(String firstName);

  @MybatisTemplate
  @Modifying
  @Transactional
  int insert(String firstName, String lastName);

  @MybatisTemplate
  @Modifying
  @Transactional
  int update(Integer id, String lastName);

  @MybatisTemplate
  @Modifying
  @Transactional
  void updateNoReturn(Integer id, String lastName);

  @MybatisTemplate
  @Modifying
  @Transactional
  int deleteMybatis(Integer id);

  @MybatisTemplate
  @Modifying
  @Transactional
  void deleteMybatisNoResturn(Integer id);
}