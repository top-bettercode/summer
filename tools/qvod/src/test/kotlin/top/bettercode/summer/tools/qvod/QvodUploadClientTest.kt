package top.bettercode.summer.tools.qvod

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.test.BaseTest

/**
 *
 * @author Peter Wu
 */
@Disabled
internal class QvodUploadClientTest : BaseTest() {

    @Autowired
    lateinit var qvodUploadClient: QvodUploadClient


    @Disabled
    @Test
    fun upload() {
        qvodUploadClient.upload(
                ClassPathResource("test.jpg").file,
                "LongVideoPreset"
        )//387702304899111623
//        qvodUploadClient.upload(ClassPathResource("test.mp4").file)//387702304900182040
    }

}