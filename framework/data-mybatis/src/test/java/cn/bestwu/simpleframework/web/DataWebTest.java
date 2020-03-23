package cn.bestwu.simpleframework.web;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataWebTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  public void test() throws Exception {
    ResponseEntity<String> entity = restTemplate
        .getForEntity("/test", String.class);
    Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}