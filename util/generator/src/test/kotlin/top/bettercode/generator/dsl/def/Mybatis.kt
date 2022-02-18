package top.bettercode.generator.dsl.def

import top.bettercode.generator.dsl.Generator
import org.mybatis.generator.api.MyBatisGenerator
import org.mybatis.generator.config.*
import org.mybatis.generator.internal.DefaultShellCallback
import java.io.File
import java.util.*

/**
 * MybatisGenerator
 *
 * @author Peter Wu
 */
class Mybatis : Generator() {
    private val context: Context = Context(ModelType.FLAT)
    private val config = Configuration()

    private val warnings = ArrayList<String>()
    private val callback = DefaultShellCallback(true)
    private val commentGeneratorConfiguration = CommentGeneratorConfiguration().apply {
        addProperty("suppressDate", "true")
        addProperty("suppressAllComments", "true")
    }
    private val javaTypeResolverConfiguration = JavaTypeResolverConfiguration().apply {
        addProperty("forceBigDecimals", "false")
    }
    private val jdbcConnectionConfiguration = JDBCConnectionConfiguration().apply {
        addProperty("remarksReporting", "true") //oracle 读取表注释
        addProperty("useInformationSchema", "true")//mysql 读取表注释
        addProperty("characterEncoding", "utf8")//mysql 读取表注释
    }
    private val javaModelGeneratorConfiguration = JavaModelGeneratorConfiguration()
    private val javaClientGeneratorConfiguration = JavaClientGeneratorConfiguration()
    private val sqlMapGeneratorConfiguration = SqlMapGeneratorConfiguration()
    private val tableConfiguration = TableConfiguration(context).apply {
        isCountByExampleStatementEnabled = true
        isDeleteByPrimaryKeyStatementEnabled = true
        isDeleteByExampleStatementEnabled = true
        isSelectByPrimaryKeyStatementEnabled = true
        isSelectByExampleStatementEnabled = true
        isUpdateByExampleStatementEnabled = true
        isUpdateByPrimaryKeyStatementEnabled = true
        isInsertStatementEnabled = true
    }

    init {
        with(config) {
            addContext(context.apply {
                id = "context"
                commentGeneratorConfiguration = this@Mybatis.commentGeneratorConfiguration
                javaTypeResolverConfiguration = this@Mybatis.javaTypeResolverConfiguration
                jdbcConnectionConfiguration = this@Mybatis.jdbcConnectionConfiguration
                javaModelGeneratorConfiguration = this@Mybatis.javaModelGeneratorConfiguration
                javaClientGeneratorConfiguration = this@Mybatis.javaClientGeneratorConfiguration
                sqlMapGeneratorConfiguration = this@Mybatis.sqlMapGeneratorConfiguration

                tableConfigurations.add(tableConfiguration)
            })
        }
    }

    override fun call() {

        jdbcConnectionConfiguration.apply {
            if (driverClass.isNullOrBlank())
                driverClass = extension.datasource.driverClass
            if (connectionURL.isNullOrBlank())
                connectionURL = extension.datasource.url
            if (userId.isNullOrBlank())
                userId = extension.datasource.username
            if (password.isNullOrBlank())
                password = extension.datasource.password
        }
        javaModelGeneratorConfiguration.apply {
            if (targetPackage.isNullOrBlank())
                targetPackage = "${extension.packageName}.domain"
            if (targetProject.isNullOrBlank())
                targetProject = extension.dir
            mkdir(targetProject, targetPackage)
        }
        javaClientGeneratorConfiguration.apply {
            if (configurationType.isNullOrBlank())
                configurationType = "ANNOTATEDMAPPER"
            if (targetPackage.isNullOrBlank())
                targetPackage = "${extension.packageName}.dao.crud"
            if (targetProject.isNullOrBlank())
                targetProject = extension.dir
            mkdir(targetProject, targetPackage)
        }
        sqlMapGeneratorConfiguration.apply {
            if (targetPackage.isNullOrBlank())
                targetPackage = "mybatis"
            if (targetProject.isNullOrBlank())
                targetProject = if (extension.dir.isBlank()) "src/main/resources" else extension.dir.replace("java", "resources")
            mkdir(targetProject, targetPackage)
        }

        tableConfiguration.apply {
            if (tableName.isNullOrBlank())
                tableName = this@Mybatis.tableName

            if (domainObjectName.isNullOrBlank())
                domainObjectName = className
        }
        val myBatisGenerator = MyBatisGenerator(config, callback, warnings)
        myBatisGenerator.generate(null)
    }

    private fun mkdir(dir: String, packageName: String) {
        File(dir, packageName.replace('.', '/')).mkdirs()
    }
}