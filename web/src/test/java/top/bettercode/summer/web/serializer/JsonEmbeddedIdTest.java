package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId;

/**
 * @author Peter Wu
 */
public class JsonEmbeddedIdTest {

  public static class User {

    @JsonEmbeddedId
    UserKey key;
    String tel;
    String password;

    public UserKey getKey() {
      return key;
    }

    public void setKey(UserKey key) {
      this.key = key;
    }

    public String getTel() {
      return tel;
    }

    public void setTel(String tel) {
      this.tel = tel;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static class UserKey implements Serializable {

    private static final long serialVersionUID = 1L;
    String id;
    String key;

    public UserKey() {
    }

    public UserKey(String key) {
      Assert.hasText(key, "key不能为空");
      String[] split = key.split(",");
      Assert.isTrue(split.length == 2, "key格式不对");
      this.id = split[0];
      this.key = split[1];
    }

    @Override
    public String toString() {
      return id + ',' + key;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }

  final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void test() throws JsonProcessingException {
    User user = new User();
    UserKey key = new UserKey("1,2");
    user.setKey(key);
    user.setPassword("1");
    user.setTel("18000000000");
    String value = objectMapper.writeValueAsString(user);
    System.err.println(value);
    System.err.println(StringUtil.valueOf(objectMapper.readValue(value, User.class)));
  }
}