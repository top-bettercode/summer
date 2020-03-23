package cn.bestwu.simpleframework.data.test.repository;

import cn.bestwu.simpleframework.data.test.domain.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

  List<User> findByMybatis();

}