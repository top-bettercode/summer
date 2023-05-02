package top.bettercode.summer.tools.sap.connection

import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 */
//@SpringBootTest(classes = TestApplication.class)
internal class SapServiceTest {
    //  @Autowired
    //  SapService sapService;
    @Test
    fun test1() {
        val s = "Integer '2027' has to many digits at field NDJAR"
        val msgRegex = "^Integer '(.*?)' has to many digits at field (.*?)$"
        System.err.println(s.replace(msgRegex.toRegex(), "$1"))
    }
}