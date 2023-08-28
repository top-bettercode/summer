package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment
import org.springframework.lang.Nullable
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dsl.def.PlantUML
import java.io.File
import java.net.URLEncoder
import java.time.LocalDate
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletResponse

/**
 * 生成数据表结构puml
 */
@Endpoint(id = "puml")
class GenEndpoint(
        private val response: HttpServletResponse,
        dataSourceProperties: DataSourceProperties? = null,
        environment: Environment,
        serverProperties: ServerProperties,
        webEndpointProperties: WebEndpointProperties
) {
    private val log: Logger = LoggerFactory.getLogger(GenEndpoint::class.java)
    private val contextPath: String = serverProperties.servlet.contextPath ?: "/"
    private val basePath: String = contextPath + webEndpointProperties.basePath + "/puml"

    companion object {
        fun databases(environment: Environment): MutableMap<String, DatabaseConfiguration> {
            return Binder.get(
                    environment
            ).bind<MutableMap<String, DataSourceProperties>>(
                    "summer.datasource.multi.datasources", Bindable
                    .mapOf(
                            String::class.java,
                            DataSourceProperties::class.java
                    )
            ).orElse(mutableMapOf()).mapValues { (_, v) ->
                val database = DatabaseConfiguration(
                        url = v.determineUrl() ?: "",
                )
                database.username = v.determineUsername() ?: ""
                database.password = v.determinePassword() ?: ""
                database.driverClass = v.determineDriverClassName() ?: ""
                database
            }.toMutableMap()
        }
    }

    private val databases: MutableMap<String, DatabaseConfiguration> = databases(environment)

    init {
        val defaultConfiguration = databases["primary"]
        if (defaultConfiguration != null) {
            databases[GeneratorExtension.DEFAULT_MODULE_NAME] = defaultConfiguration
            databases.remove("primary")
        } else {
            try {
                if (dataSourceProperties != null) {
                    val configuration = DatabaseConfiguration()
                    configuration.url = dataSourceProperties.determineUrl() ?: ""
                    configuration.username = dataSourceProperties.determineUsername() ?: ""
                    configuration.password = dataSourceProperties.determinePassword() ?: ""
                    configuration.driverClass =
                            dataSourceProperties.determineDriverClassName() ?: ""

                    databases[GeneratorExtension.DEFAULT_MODULE_NAME] = configuration
                }
            } catch (_: Exception) {
            }
        }
    }

    @ReadOperation
    fun root() {
        response.contentType = "text/html; charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setDateHeader("Expires", 0)
        response.writer.use { writer ->
            writer.println(
                    """
<html>
<head><title>Index of database</title></head>
<body>"""
            )
            writer.print("<h1>Index of database</h1><hr><pre>")

            for (key in databases.keys) {
                writer.println("<a href=\"$basePath/$key\">$key.puml</a>")
            }
            writer.println("</pre><hr></body>\n</html>")
        }
    }

    @ReadOperation
    fun puml(@Selector module: String, @Nullable force: Boolean?) {
        log.info("开始生成puml:{}", module)
        val tmpPath = System.getProperty("java.io.tmpdir")
        val destFile = File(tmpPath, "summer/puml/${module}-${LocalDate.now()}.puml")
        if (force == true || !destFile.exists()) {
            val database = databases[module]
                    ?: throw IllegalArgumentException("module $module not found")
            val tables = database.tables()
            log.info("tables:{}", tables.map { it.tableName })
            val plantUML = PlantUML(
                    tables[0].subModuleName,
                    destFile,
                    null
            )
            plantUML.setUp(GeneratorExtension())
            tables.sortedBy { it.tableName }.forEach { table ->
                plantUML.run(table)
            }
            plantUML.tearDown()
        }
        log.info("puml:{}", destFile)
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=${module}.puml;filename*=UTF-8''" + URLEncoder.encode(
                        "${module}.puml",
                        "UTF-8"
                )
        )
        response.contentType = "application/octet-stream; charset=utf-8"
        response.setHeader("Pragma", "No-cache")
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Content-Encoding", "gzip")
        response.setDateHeader("Expires", 0)

        GZIPOutputStream(response.outputStream).buffered().use { bos ->
            bos.write(destFile.inputStream().readBytes())
        }
    }


}
