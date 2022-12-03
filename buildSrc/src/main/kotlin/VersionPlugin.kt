import org.gradle.api.Project
import java.util.*


private val summerVersionConfig = ResourceBundle.getBundle("summer-version")
val Project.versionConfig: Map<String, String>
    get() = summerVersionConfig.keys.toList().associateWith { summerVersionConfig.getString(it) }

val Project.oracleJdbcVersion: String get() = summerVersionConfig.getString("oracle-jdbc.version")

val Project.summerVersion: String get() = summerVersionConfig.getString("summer.version")
val Project.dokkaVersion: String get() = summerVersionConfig.getString("dokka.version")
val Project.kotlinxCoroutinesVersion: String get() = summerVersionConfig.getString("kotlinx-coroutines.version")
