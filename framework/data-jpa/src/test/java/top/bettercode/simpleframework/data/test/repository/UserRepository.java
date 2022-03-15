package top.bettercode.simpleframework.data.test.repository;

import java.util.List;
import java.util.Map;
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

public interface UserRepository extends JpaExtRepository<User, Integer>,
    QuerydslPredicateExecutor<User>,
    RecycleQuerydslPredicateExecutor<User> {

  List<User> findByLastName(String lastName);

  Page<User> findByFirstName(String lastName, Pageable pageable);

  @Modifying
  @Transactional
  void deleteByLastName(String lastName);

  List<User> findByMybatis22(User user);


  @MybatisTemplate
  List<User> findByMybatis();

  Page<User> findByMybatis(Pageable pageable);

  @Select("select * from user where deleted = 0")
  List<User> findByMybatisSize(Size size);

  List<User> findByMybatis257(String firstName, String lastName);

  List<User> findByMybatis2(Map<String, String> param);

  Page<User> findByMybatis2(Pageable pageable, Map<String, String> param);


  Page<User> findByMybatis22(User user, Pageable pageable);

  @Query(value = "select * from user where first_name = ?1", nativeQuery = true)
  Page<User> findBy22(String first_name, Pageable pageable);

  List<User> findByMybatis222(User user, Pageable pageable);

  List<User> findByMybatis3(String firstName, Sort sort);

  Page<User> findByMybatis3(String firstName, Pageable pageable);

  User findOneByMybatis(String firstName);

  @Modifying
  int insert(String firstName, String lastName);

  @Modifying
  int update(Integer id, String lastName);

  @Modifying
  int deleteBy(Integer id);
}