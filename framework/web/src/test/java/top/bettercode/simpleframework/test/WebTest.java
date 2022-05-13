package top.bettercode.simpleframework.test;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class WebTest {

  @Autowired
  TestRestTemplate restTemplate;
  @Autowired
  Environment environment;
  @Value("${logging.level.org.springframework.boot.SpringApplication}")
  String logLevel;

  @Test
  void env() {
    System.err.println(logLevel);
  }

  @Test
  public void test() {
    ResponseEntity<String> entity = restTemplate
        .getForEntity("/test?price=12&cent=22&a=1585549626000&cell=18221161113&number1=1", String.class);
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }

  @Test
  public void optionserror() {
    Set<HttpMethod> httpMethods = restTemplate.optionsForAllow("/errors");
    System.out.println(httpMethods);
  }


  @Test
  public void error() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    ResponseEntity<String> entity = restTemplate
        .postForEntity("/errors", new HttpEntity<>(headers), String.class);
    System.err.println(entity.getBody());
    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
  }
}