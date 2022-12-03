package top.bettercode.summer.web.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.web.serializer.annotation.JsonHide;

/**
 * @author Peter Wu
 */
public class HideSerializerTest {

  static class User {

    @JsonHide(beginKeep = 2, endKeep = 2)
    String tel;
    @JsonHide(beginKeep = 1, endKeep = 1)
    String password;

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

  final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void test() throws JsonProcessingException {
    User user = new User();
    user.setPassword("1");
    user.setTel("18000000000");
    System.err.println(StringUtil.valueOf(objectMapper.writeValueAsString(user)));

  }
}