package top.bettercode.summer.web.validator

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object IDCardUtil {
    // 每位加权因子
    private val power = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
            8, 4, 2)

    // 第18位校检码
    private val verifyCode = arrayOf("1", "0", "X", "9", "8", "7", "6",
            "5", "4", "3", "2")

    /**
     *
     *
     * 判断18位身份证的合法性
     *
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     *
     *
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配给女性。
     *
     *
     *
     * 1.前1、2位数字表示：所在省份的代码； 2.第3、4位数字表示：所在城市的代码； 3.第5、6位数字表示：所在区县的代码； 4.第7~14位数字表示：出生年、月、日；
     * 5.第15、16位数字表示：所在地的派出所的代码； 6.第17位数字表示性别：奇数表示男性，偶数表示女性；
     * 7.第18位数字是校检码：也有的说是个人信息码，一般是随计算机的随机产生，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示。
     *
     *
     *
     * 第十八位数字(校验码)的计算方法为： 1.将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2
     *
     *
     *
     * 2.将这17位数字和系数相乘的结果相加。
     *
     *
     *
     * 3.用加出来和除以11，看余数是多少？
     *
     * 4.余数只可能有 0 1 2 3 4 5 6 7 8 9 10 这11个数字。其分别对应的最后一位身份证的号码为 1 0 X 9 8 7 6 5 4 3 2。
     *
     *
     * 5.通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2。
     *
     *
     * @param idcard 身份证
     * @return 是否合法
     */
    fun validate(idcard: String?): Boolean {
        var id = idcard ?: return false
        if (id.length == 15) {
            id = convertFrom15bit(id)?:return false
        }

        // 非18位为假
        if (id.length != 18) {
            return false
        }
        // 获取前17位
        val idcard17 = id.substring(0, 17)
        // 获取第18位
        val idcard18Code = id.substring(17, 18)
        val c: CharArray
        val checkCode: String
        // 是否都为数字
        return if (isDigital(idcard17)) {
            c = idcard17.toCharArray()
            val bit: IntArray = converCharToInt(c)
            val sum17: Int = getPowerSum(bit)

            // 将和值与11取模得到余数进行校验码判断
            checkCode = getCheckCodeBySum(sum17)
            idcard18Code.equals(checkCode, ignoreCase = true)
            // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
        } else {
            false
        }
    }

    /**
     * 将15位的身份证转成18位身份证
     *
     * @param idcard 15位的身份证
     * @return 18位身份证
     */
    fun convertFrom15bit(idcard: String): String? {
        var idcard17: String
        // 非15位身份证
        if (idcard.length != 15) {
            return null
        }
        return if (isDigital(idcard)) {
            try {
                // 获取出生年月日
                val birthday = idcard.substring(6, 12)
                idcard17 = idcard.substring(0, 6) + LocalDate
                        .parse(birthday, DateTimeFormatter.ofPattern("yyMMdd")).year + idcard.substring(8)
                val c = idcard17.toCharArray()
                val checkCode: String

                // 将字符数组转为整型数组
                val bit: IntArray = converCharToInt(c)
                val sum17: Int = getPowerSum(bit)

                // 获取和值与11取模得到余数进行校验码
                checkCode = getCheckCodeBySum(sum17)
                // 获取不到校验位

                // 将前17位与第18位校验码拼接
                idcard17 += checkCode
                idcard17
            } catch (e: NumberFormatException) {
                null
            }
        } else { // 身份证包含数字
            null
        }
    }

    /**
     * 数字验证
     */
    private fun isDigital(str: String?): Boolean {
        return !(str == null || "" == str) && str.matches("^[0-9]*$".toRegex())
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     */
    private fun getPowerSum(bit: IntArray): Int {
        var sum = 0
        if (power.size != bit.size) {
            return 0
        }
        for (i in bit.indices) {
            for (j in power.indices) {
                if (i == j) {
                    sum += bit[i] * power[j]
                }
            }
        }
        return sum
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @return 校验位
     */
    private fun getCheckCodeBySum(sum17: Int): String {
        return verifyCode[sum17 % 11]
    }

    /**
     * 将字符数组转为整型数组
     */
    @Throws(NumberFormatException::class)
    private fun converCharToInt(c: CharArray): IntArray {
        val a = IntArray(c.size)
        var k = 0
        for (temp in c) {
            a[k++] = temp.toString().toInt()
        }
        return a
    }
}