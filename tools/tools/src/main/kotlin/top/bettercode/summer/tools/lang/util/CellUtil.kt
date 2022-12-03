package top.bettercode.summer.tools.lang.util

import top.bettercode.summer.tools.lang.property.Settings


/**
 * 手机号工具类
 *
 *
 * 其他号段
 * 14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。
 * 170号段为虚拟运营商专属号段，170号段的 11 位手机号前四位来区分基础运营商，其中 “1700” 为中国电信的转售号码标识，“1705” 为中国移动，“1709” 为中国联通。
 * 171号段也为虚拟运营商专属号段。
 * 卫星通信 1349
 *
 * 20170808 工信部新批号段：电信199/移动198/联通166 ，146联通，148移动，1740、1741电信和工信部的卫星通信号段，144十三位移动物联网，141十三位电信物联网，10098 船舶通信导航公司客服电话
 * @author Peter Wu
 */
object CellUtil {

    enum class Model {
        ALL, SIMPLE, MOBILE, UNICOM, TELECOM, VNO
    }

    /**
     * 中国移动号段
     * 2G号段（GSM网络）有134x（0-8）、135、136、137、138、139、150、151、152、158、159、182、183、184。
     * 3G号段（TD-SCDMA网络）有157、187、188
     * 3G上网卡 147
     * 4G号段 178、184
     * 新 198 148 144 172
     */
    private const val CHINA_MOBILE_CELL_REGEX =
        "^(144\\d|148\\d|198\\d|134\\d|135\\d|136\\d|137\\d|138\\d|139\\d|150\\d|151\\d|152\\d|157\\d|158\\d|159\\d|182\\d|183\\d|184\\d|187\\d|172\\d|178\\d|188\\d|147\\d|1705)\\d{7}$"

    /**
     * 中国联通号段
     * 2G号段（GSM网络）130、131、132、155、156
     * 3G上网卡145
     * 3G号段（WCDMA网络）185、186
     * 4G号段 176、185[1]
     * 新 166 146
     */
    private const val CHINA_UNICOM_CELL_REGEX =
        "^(146\\d|166\\d|130\\d|131\\d|132\\d|155\\d|156\\d|175\\d|176\\d|185\\d|186\\d|1709)\\d{7}$"

    /**
     * 中国电信号段
     * 2G/3G号段（CDMA2000网络）133、153、180、181、189
     * 4G号段 177、173
     * 新 199 141
     * 1740、1741电信和工信部的卫星通信号段
     */
    private const val CHINA_TELECOM_CELL_REGEX =
        "^(141\\d|199\\d|133\\d|153\\d|173\\d|177\\d|180\\d|181\\d|189\\d|191\\d|1700|1740|1741)\\d{7}$"

    /**
     * 中国虚拟运营商号段
     */
    private const val CHINA_VNO_CELL_REGEX = "^(170|171)\\d{8}$"

    private val cellRegexes = Settings.cellRegex

    private const val CHINA_MOBILE_CELL_REGEX_KEY = "regexp.china_mobile_cell"
    private const val CHINA_UNICOM_CELL_REGEX_KEY = "regexp.china_unicom_cell"
    private const val CHINA_TELECOM_CELL_REGEX_KEY = "regexp.china_telecom_cell"
    private const val CHINA_VNO_CELL_REGEX_KEY = "regexp.china_vno_cell"

    init {
        cellRegexes.putIfAbsent(CHINA_MOBILE_CELL_REGEX_KEY, CHINA_MOBILE_CELL_REGEX)
        cellRegexes.putIfAbsent(CHINA_UNICOM_CELL_REGEX_KEY, CHINA_UNICOM_CELL_REGEX)
        cellRegexes.putIfAbsent(CHINA_TELECOM_CELL_REGEX_KEY, CHINA_TELECOM_CELL_REGEX)
        cellRegexes.putIfAbsent(CHINA_VNO_CELL_REGEX_KEY, CHINA_VNO_CELL_REGEX)
    }

    /**
     * 是否为中国移动号码
     *
     * @param cell 手机号码
     * @return 是否为中国移动号码
     */
    @JvmStatic
    fun isChinaMobile(cell: String?): Boolean {
        return cell?.matches(cellRegexes[CHINA_MOBILE_CELL_REGEX_KEY]!!.toRegex()) ?: false
    }

    /**
     * 是否为中国联通号码
     *
     * @param cell 手机号码
     * @return 是否为中国联通号码
     */
    @JvmStatic
    fun isChinaUnicom(cell: String?): Boolean {
        return cell?.matches(cellRegexes[CHINA_UNICOM_CELL_REGEX_KEY]!!.toRegex()) ?: false
    }

    /**
     * 是否为中国电信号码
     *
     * @param cell 手机号码
     * @return 是否为中国电信号码
     */
    @JvmStatic
    fun isChinaTelecom(cell: String?): Boolean {
        return cell?.matches(cellRegexes[CHINA_TELECOM_CELL_REGEX_KEY]!!.toRegex()) ?: false
    }

    /**
     * 是否为中国虚拟运营商号码
     *
     * @param cell 手机号码
     * @return 是否为中国虚拟运营商号码
     */
    @JvmStatic
    fun isChinaVNO(cell: String?): Boolean {
        return cell?.matches(cellRegexes[CHINA_VNO_CELL_REGEX_KEY]!!.toRegex()) ?: false
    }

    /**
     * 是否为中国内地手机号
     *
     * @param cell 手机号码
     * @return 是否为中国内地手机号
     */
    @JvmStatic
    fun isChinaCell(cell: String?): Boolean {
        return if (cell == null) {
            false
        } else {
            isChinaMobile(cell) || isChinaUnicom(cell) || isChinaTelecom(cell) || isChinaVNO(cell)
        }
    }

    /**
     * 是否为中国内地手机号
     *
     * @param cell 手机号码
     * @return 是否为中国内地手机号
     */
    @JvmStatic
    fun isSimpleCell(cell: String?): Boolean {
        return cell?.matches(Regex("^[1-9]\\d{10}$")) ?: false
    }
}
