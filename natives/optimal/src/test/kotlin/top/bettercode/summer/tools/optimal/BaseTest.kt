package top.bettercode.summer.tools.optimal

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import copt.Consts
import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class BaseTest {

    @Test
    fun test() {
        System.err.println(Consts.INFINITY.toBigDecimal().toPlainString())
        Loader.loadNativeLibraries()
        System.err.println(MPSolver.infinity())
    }


}