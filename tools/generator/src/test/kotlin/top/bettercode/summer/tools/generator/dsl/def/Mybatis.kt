package top.bettercode.summer.tools.generator.dsl.def

import org.mybatis.generator.api.MyBatisGenerator
import org.mybatis.generator.config.*
import org.mybatis.generator.internal.DefaultShellCallback
import top.bettercode.summer.tools.generator.dsl.Generator
import java.io.File

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
                setJdbcConnectionConfiguration(this@Mybatis.jdbcConnectionConfiguration)
                javaModelGeneratorConfiguration = this@Mybatis.javaModelGeneratorConfiguration
                javaClientGeneratorConfiguration = this@Mybatis.javaClientGeneratorConfiguration
                sqlMapGeneratorConfiguration = this@Mybatis.sqlMapGeneratorConfiguration

                addTableConfiguration(tableConfiguration)
            })
        }
    }

    override fun call() {

        jdbcConnectionConfiguration.apply {
            if (driverClass.isNullOrBlank())
                driverClass = datasource.driverClass
            if (connectionURL.isNullOrBlank())
                connectionURL = datasource.url
            if (userId.isNullOrBlank())
                userId = datasource.username
            if (password.isNullOrBlank())
                password = datasource.password
        }
        javaModelGeneratorConfiguration.apply {
            if (targetPackage.isNullOrBlank())
                targetPackage = "${ext.packageName}.domain"
            if (targetProject.isNullOrBlank())
                targetProject = ext.dir
            mkdir(targetProject, targetPackage)
        }
        javaClientGeneratorConfiguration.apply {
            if (configurationType.isNullOrBlank())
                configurationType = "ANNOTATEDMAPPER"
            if (targetPackage.isNullOrBlank())
                targetPackage = "${ext.packageName}.dao.crud"
            if (targetProject.isNullOrBlank())
                targetProject = ext.dir
            mkdir(targetProject, targetPackage)
        }
        sqlMapGeneratorConfiguration.apply {
            if (targetPackage.isNullOrBlank())
                targetPackage = "mybatis"
            if (targetProject.isNullOrBlank())
                targetProject =
                    if (ext.dir.isBlank()) "src/main/resources" else ext.dir.replace(
                        "java",
                        "resources"
                    )
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