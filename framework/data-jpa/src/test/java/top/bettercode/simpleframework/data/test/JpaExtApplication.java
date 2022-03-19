package top.bettercode.simpleframework.data.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.EnableJpaExtRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaExtRepositories
@SpringBootApplication
@EnableTransactionManagement
public class JpaExtApplication {

  public static void main(String[] args) {
    SpringApplication.run(JpaExtApplication.class, args);
  }

}