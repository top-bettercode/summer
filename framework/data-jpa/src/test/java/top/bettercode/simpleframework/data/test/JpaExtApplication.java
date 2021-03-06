package top.bettercode.simpleframework.data.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.bettercode.simpleframework.data.jpa.config.EnableJpaExtRepositories;

@EnableJpaExtRepositories
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaExtApplication {

  public static void main(String[] args) {
    SpringApplication.run(JpaExtApplication.class, args);
  }

}