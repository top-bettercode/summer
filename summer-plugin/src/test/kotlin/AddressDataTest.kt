import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */

class AddressDataTest {

    @Test
    fun testtrim() {
        val trim = "??. M: F:; O:?".replace("?", "").trim('.').trim()
        System.err.println("[$trim]")
    }

    @Test
    fun test() {
        val tableName = "ac_area"
        val dest = File("build/ac_area.sql")
        dest.printWriter().use { p ->
            p.println("delete from $tableName;")
            val addressData = Properties()
            addressData.load(
                AddressDataTest::class.java.classLoader.getResourceAsStream("address-data.properties")
            )
            addressData.keys.sortedBy { any -> any.toString() }.forEach {
                val code = it.toString()
                val name = getName(addressData, code)!!
                val rootCode = "${code.subSequence(0, 2)}0000"
                val rootName = getName(addressData, rootCode)!!
                val parentCode: String
                val path: String?
                val level: Int
                when {
                    code.endsWith("0000") -> {
                        parentCode = "-1"
                        path = name
                        level = 1
                    }

                    code.endsWith("00") -> {
                        parentCode = rootCode
                        path = "$rootName>$name"
                        level = 2
                    }

                    else -> {
                        parentCode = "${code.subSequence(0, 4)}00"
                        var pname = getName(addressData, parentCode)
                        if (pname == null) {
                            pname = if (name.endsWith("县")) {
                                if (rootName.endsWith("省"))
                                    "直辖县"
                                else
                                    "市辖县"
                            } else if (rootName.endsWith("市")) {
                                "市辖区"
                            } else {
                                "直辖县"
                            }

                            addressData[parentCode] = pname
                            p.println("INSERT INTO $tableName (area_code, area_name, area_path, area_parent_code, area_level) VALUES ('$parentCode', '$pname', '${"$rootName>$pname"}', '$rootCode', 2);")
                        }
                        path = "$rootName>$pname>$name"
                        level = 3
                    }
                }

                p.println("INSERT INTO $tableName (area_code, area_name, area_path, area_parent_code, area_level) VALUES ('$code', '$name', '$path', '$parentCode', $level);")
            }
        }
    }

    private fun getName(addressData: Properties, parentCode: String) =
        (addressData[parentCode] as? String)?.trim()
}