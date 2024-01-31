package top.bettercode.summer.tools.recipe

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
class COPTFileTest {
    @Test
    fun read() {
        val env = copt.COPTEnvr()
        val model = env.createModel("")
        model.readMps("test.mps")
        model.solve()
        println(StringUtil.valueOf(model.solution))
    }

}