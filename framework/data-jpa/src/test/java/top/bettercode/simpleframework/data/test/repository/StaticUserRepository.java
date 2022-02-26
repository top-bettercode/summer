package top.bettercode.simpleframework.data.test.repository;

import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.test.domain.StaticUser;
import java.util.List;

public interface StaticUserRepository extends JpaExtRepository<StaticUser, Integer> {

  List<StaticUser> findByLastName(String lastName);
}