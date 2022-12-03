package top.bettercode.summer.tools.sap.connection;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 */
//@SpringBootTest(classes = TestApplication.class)
class SapServiceTest {

//  @Autowired
//  SapService sapService;

  @Test
  void test1() {
    String s = "Integer '2027' has to many digits at field NDJAR";
    String msgRegex = "^Integer '(.*?)' has to many digits at field (.*?)$";
    System.err.println(s.replaceAll(msgRegex, "$1"));
  }
}