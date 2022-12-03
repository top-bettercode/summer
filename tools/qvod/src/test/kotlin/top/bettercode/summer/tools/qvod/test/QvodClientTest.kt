package top.bettercode.summer.tools.qvod.test

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import top.bettercode.summer.test.BaseTest
import top.bettercode.summer.tools.qvod.QvodClient

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

    @Disabled
    @Test
    fun processMediaByProcedure() {
        qvodClient.processMediaByProcedure("", "")
    }

    @Disabled
    @Test
    fun deleteMedia() {
        qvodClient.deleteMedia("387702304899113118")
    }

    @Disabled
    @Test
    fun processMedia() {
        qvodClient.processMedia("387702304900182040")
        //{
        //  "taskId" : "1313291945-procedurev2-8fbc7e646955b66e751a585153cc5706tt0",
        //  "requestId" : "824a12f8-306b-4cda-ae9e-6697bb4672e5"
        //}
    }

    @Disabled
    @Test
    fun reviewImage() {
        qvodClient.reviewImage("387702304899111623")
    }

    @Disabled
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

    @Disabled
    @Test
    fun confirmEvents() {
        qvodClient.confirmEvents("tdmq830894935510738067")
    }

    @Disabled
    @Test
    fun describeMediaInfo() {
        qvodClient.describeMediaInfo("387702306840111681", "metaData")
    }

    @Disabled
    @Test
    fun playSignature() {
        val signature = qvodClient.playSignature("387702307226359579")
        System.err.println(signature)
    }

    @Test
    fun antiLeechUrl() {
        var antiLeechUrl =
            qvodClient.antiLeechUrl("https://1313291945.vod2.myqcloud.com/3306e890vodtranscq1313291945/7a0382da387702307226359579/v.f80000.mp4")
        System.err.println(antiLeechUrl)
        antiLeechUrl = qvodClient.antiLeechUrl(antiLeechUrl)
        System.err.println(antiLeechUrl)
    }
}