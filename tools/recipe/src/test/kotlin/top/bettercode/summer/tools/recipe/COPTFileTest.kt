package top.bettercode.summer.tools.recipe

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil

/**
 *
 * @author Peter Wu
 */
@Disabled
class COPTFileTest {
    @Test
    fun read() {
        val env = copt.Envr()
        val model = env.createModel("")
//        model.setIntParam(copt.IntParam.Presolve, 0)
        model.readMps("build/test.mps")
        model.solve()
        println(StringUtil.valueOf(model.solution))
    }

}