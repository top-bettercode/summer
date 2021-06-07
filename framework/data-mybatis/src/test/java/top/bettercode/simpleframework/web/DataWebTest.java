package top.bettercode.simpleframework.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataWebTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  public void test() {
    ResponseEntity<String> entity = restTemplate
        .getForEntity("/test", String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}