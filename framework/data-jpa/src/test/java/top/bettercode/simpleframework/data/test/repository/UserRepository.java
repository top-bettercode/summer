package top.bettercode.simpleframework.data.test.repository;

import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.test.domain.User;
import top.bettercode.simpleframework.data.jpa.querydsl.RecycleQuerydslPredicateExecutor;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.mybatis.MybatisTemplate;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UserRepository extends JpaExtRepository<User, Integer>,
    QuerydslPredicateExecutor<User>,
    RecycleQuerydslPredicateExecutor<User> {

  List<User> findByLastname(String lastname);

  Page<User> findByFirstname(String lastname, Pageable pageable);

  @Modifying
  @Transactional
  void deleteByLastname(String lastname);

  @MybatisTemplate
  List<User> findByMybatis();

  List<User> findByMybatis2(String firstname, String lastname);

  List<User> findByMybatis2(Map<String, String> param);

  Page<User> findByMybatis2(Pageable pageable, Map<String, String> param);

  List<User> findByMybatis22(User user);

  Page<User> findByMybatis22(User user, Pageable pageable);

  List<User> findByMybatis222(User user, Pageable pageable);

  List<User> findByMybatis3(String firstname, Sort sort);

  Page<User> findByMybatis3(String firstname, Pageable pageable);

  User findOneByMybatis(String firstname);

  @Modifying
  int insert(String firstname, String lastname);

  @Modifying
  int update(Integer id, String lastname);

  @Modifying
  int deleteBy(Integer id);
}