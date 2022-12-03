package top.bettercode.summer.data.jpa.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import top.bettercode.summer.data.jpa.JpaExtRepository;
import top.bettercode.summer.data.jpa.domain.User;
import top.bettercode.summer.data.jpa.querydsl.RecycleQuerydslPredicateExecutor;
import top.bettercode.summer.data.jpa.resp.AUser;
import top.bettercode.summer.data.jpa.resp.CUser;
import top.bettercode.summer.data.jpa.resp.CUsers;
import top.bettercode.summer.data.jpa.support.QueryFirst;
import top.bettercode.summer.data.jpa.support.Size;

public interface UserRepository extends JpaExtRepository<User, Integer>,
    QuerydslPredicateExecutor<User>,
    RecycleQuerydslPredicateExecutor<User> {

  List<User> findByLastName(String lastName);

  Page<User> findByFirstName(String lastName, Pageable pageable);

  @Query(value = "select * from t_user where first_name = ?1", nativeQuery = true)
  Page<User> selectNativeSql(String first_name, Pageable pageable);


  @Transactional
  void deleteByLastName(String lastName);

  //--------------------------------------------

  List<CUser> selectResultMap(User user);

  List<CUsers> selectResultMap2(User user);

//  Page<CUsers> selectResultMap2(User user, Pageable pageable);

  List<AUser> selectResultMap3(User user);

  AUser selectResultOne3(User user);

  @QueryFirst
  AUser selectResultFirst3(User user);

  Page<AUser> selectResultMap3(User user, Pageable pageable);

  List<User> selectMybatisAll();

  List<Map<String, String>> selectMybatisMapList();

  Map<String, String> selectMybatisMap();

  Page<User> selectMybatisAll(Pageable pageable);

  @Select("select * from t_user where deleted = 0")
  List<User> selectByMybatisSize(Size size);

  List<User> selectMybatisIfParam(String firstName, String lastName);

  Stream<User> selectMybatisStream(String firstName, String lastName);

  List<User> selectByMybatisMap(Map<String, String> param);

  Page<User> selectByMybatisMap(Pageable pageable, Map<String, String> param);

  List<User> selectByMybatisEntity(User user, Pageable pageable);

  List<User> selectByMybatisSort(String firstName, Sort sort);

  Page<User> selectByMybatisSort(String firstName, Pageable pageable);

  User selectOneByMybatis(String firstName);

  @Transactional
  int insert(String firstName, String lastName);

  @Transactional
  int update(Integer id, String lastName);

  @Transactional
  void updateNoReturn(Integer id, String lastName);

  @Transactional
  int deleteMybatis(Integer id);

  @Transactional
  void deleteMybatisNoResturn(Integer id);
}