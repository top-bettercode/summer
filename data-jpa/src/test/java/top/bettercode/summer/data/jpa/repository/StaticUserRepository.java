package top.bettercode.summer.data.jpa.repository;

import java.util.List;
import top.bettercode.summer.data.jpa.JpaExtRepository;
import top.bettercode.summer.data.jpa.domain.StaticUser;

public interface StaticUserRepository extends JpaExtRepository<StaticUser, Integer> {

  List<StaticUser> findByLastName(String lastName);
}