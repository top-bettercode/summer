import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Wu
 */
public class StringTest {


  @org.junit.jupiter.api.Test
  void test() {
    String val = "ab@test@124@test1@cd";
    Pattern regrex = Pattern.compile("@(.+?)@");
    Matcher matcher = regrex.matcher(val);
    while (matcher.find()) {
      String group = matcher.group();
      System.err.println(group);
    }
  }
}
