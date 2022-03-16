package top.bettercode.simpleframework.data.test.repository;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import top.bettercode.simpleframework.data.test.domain.User;

/**
 * @author Peter Wu
 */
@org.springframework.stereotype.Service
public class Service {

  @Autowired
  UserRepository repository;
  @Autowired
  SqlSession sqlSession;

  @Transactional
  public void testService() {
    List<User> all = repository.findAll();
    int size = all.size();
    System.err.println(size);
    List<User> byMybatis = repository.selectMybatisAll();
    int size1 = byMybatis.size();
    System.err.println(size1);
    Assert.isTrue(size == size1, "查询结果不一致");
    User dave = new User("Dave", "Matthews");
    repository.save(dave);
    List<Object> users1 = sqlSession
        .selectList(UserRepository.class.getName() + ".selectMybatisAll");
    System.err.println(users1.size());
    all = repository.findAll();
    int size2 = all.size();
    System.err.println(size2);
    byMybatis = repository.selectMybatisAll();
    int size3 = byMybatis.size();
    System.err.println(size3);
    Assert.isTrue(size2 == size3, "查询结果不一致");

    Assert.isTrue(size2 == users1.size() + 1, "查询结果一致");

  }
}
