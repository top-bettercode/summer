package top.bettercode.summer.tools.qvod.test

import top.bettercode.summer.tools.qvod.QvodAntiLeechUrl

class DataBean {
    @QvodAntiLeechUrl
    var path = "https://vod2.myqcloud.com/ad/dd/a.mp4"

    @QvodAntiLeechUrl(separator = ",")
    var path1 = "https://vod2.myqcloud.com/ad/dd/a.mp4,https://vod2.myqcloud.com/ad/dd/b.mp4"

    @QvodAntiLeechUrl
    var paths = listOf("https://vod2.myqcloud.com/ad/dd/a.mp4",
            "https://vod2.myqcloud.com/ad/dd/b.mp4")

    @QvodAntiLeechUrl
    var pathArray = arrayOf(
            "https://vod2.myqcloud.com/ad/dd/a.mp4", "https://vod2.myqcloud.com/ad/dd/b.mp4"
    )
}