package top.bettercode.summer.util.qvod

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.util.test.BaseTest

/**
 *
 * @author Peter Wu
 */
class QvodClientTest : BaseTest() {

    @Autowired
    lateinit var qvodClient: QvodClient

    @Test
    fun signature() {
        val signature = qvodClient.signature()
        System.err.println(signature)
    }

    @Test
    fun storageRegions() {
        qvodClient.storageRegions()
    }


    @Test
    fun upload() {
//        qvodClient.upload(ClassPathResource("test.jpg").file)//387702304899111623
        qvodClient.upload(ClassPathResource("test.mp4").file)//387702304900182040
    }

    @Test
    fun deleteMedia() {
        qvodClient.deleteMedia("387702304899113118")
    }

    @Test
    fun processMedia() {
        qvodClient.processMedia("387702304900182040")
        //{
        //  "taskId" : "1313291945-procedurev2-8fbc7e646955b66e751a585153cc5706tt0",
        //  "requestId" : "824a12f8-306b-4cda-ae9e-6697bb4672e5"
        //}
    }

    @Test
    fun reviewImage() {
        qvodClient.reviewImage("387702304899111623")
    }

    @Test
    fun taskDetail() {
        qvodClient.taskDetail("1313291945-procedurev2-76faf54c22414dc08b880ecfc6b83a2att0")
    }

    @Test
    fun pullEvents() {
        val pullEvents = qvodClient.pullEvents()
        for (eventContent in pullEvents.eventSet) {
            System.err.println(eventContent.eventHandle)
        }
    }

    @Test
    fun confirmEvents() {
        qvodClient.confirmEvents("tdmq830894935510738067")
    }
}