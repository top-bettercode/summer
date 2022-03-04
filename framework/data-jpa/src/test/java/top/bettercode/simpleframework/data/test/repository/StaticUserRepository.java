package top.bettercode.simpleframework.data.test.repository;

import java.util.List;
import top.bettercode.simpleframework.data.jpa.JpaExtRepository;
import top.bettercode.simpleframework.data.test.domain.StaticUser;

public interface StaticUserRepository extends JpaExtRepository<StaticUser, Integer> {

  List<StaticUser> findByLastName(String lastName);
}