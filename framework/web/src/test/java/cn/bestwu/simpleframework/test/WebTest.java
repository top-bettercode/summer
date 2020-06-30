package cn.bestwu.simpleframework.test;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"app.web.ok.enable=true","app.web.wrap.enable=true"})
public class WebTest {

  @Autowired
  TestRestTemplate restTemplate;
  @Autowired
  Environment environment;

  @Test
  public void test() {
    ResponseEntity<String> entity = restTemplate
        .getForEntity("/test?price=1.2&cent=2.2", String.class);
    Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void error() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    ResponseEntity<String> entity = restTemplate
        .postForEntity("/errors", new HttpEntity<>(headers), String.class);
    System.err.println(entity.getBody());
    Assert.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}