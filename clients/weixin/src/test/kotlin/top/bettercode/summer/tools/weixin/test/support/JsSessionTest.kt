package top.bettercode.summer.tools.weixin.test.support

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.JsSession

/**
 *
 * @author Peter Wu
 */
class JsSessionTest {

    @Test
    fun decrypt() {
        //h5dr6I31PeLqyHodxCKOofbI0qfJqS1vsxvxo79ofyYDvCUVmbwEdwhC3y+1lbvjXJRxFc335k7iNL9bIKSp7zi9Lo2/eVi50PmnRFhadUNBnwb1jkHSuNoaPFtYonMOdwTM/CzXsFdSYkEjERn/B2ORyiMnlB/D8a5t54ZGJnEFlkkdO8S1Uts6vTzAzRiXEXq5jT1BUThLb4tUDiSakg==&iv=Nfpy+hCGmYGKko/fETxMiQ==
        val jsSession = JsSession("oTNLC5QzdR0w_Ru7OuSBIcofKkVs", "B3eer8hAvrlU3d4amFOyow==")
        jsSession.decrypt("h5dr6I31PeLqyHodxCKOofbI0qfJqS1vsxvxo79ofyYDvCUVmbwEdwhC3y+1lbvjXJRxFc335k7iNL9bIKSp7zi9Lo2/eVi50PmnRFhadUNBnwb1jkHSuNoaPFtYonMOdwTM/CzXsFdSYkEjERn/B2ORyiMnlB/D8a5t54ZGJnEFlkkdO8S1Uts6vTzAzRiXEXq5jT1BUThLb4tUDiSakg==", "Nfpy+hCGmYGKko/fETxMiQ==")
        System.err.println(StringUtil.json(jsSession, true))
    }
}