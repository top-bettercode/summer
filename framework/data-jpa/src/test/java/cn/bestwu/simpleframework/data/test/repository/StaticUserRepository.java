package cn.bestwu.simpleframework.data.test.repository;

import cn.bestwu.simpleframework.data.jpa.JpaExtRepository;
import cn.bestwu.simpleframework.data.test.domain.StaticUser;
import java.util.List;

public interface StaticUserRepository extends JpaExtRepository<StaticUser, Integer> {

  List<StaticUser> findByLastname(String lastname);
}