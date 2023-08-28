package top.bettercode.summer.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.dsl.def.PlantUML
import java.io.File
import java.net.URLEncoder
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
    private val contextPath: String = serverProperties.servlet.contextPath ?: "/"
    private val basePath: String = contextPath + webEndpointProperties.basePath + "/puml"

    private val datasources: MutableMap<String, DatabaseConfiguration> = Binder.get(
            environment
    ).bind<MutableMap<String, DatabaseConfiguration>>(
            "summer.datasource.multi.datasources", Bindable
            .mapOf(
                    String::class.java,
                    DatabaseConfiguration::class.java
            )
    ).orElse(mutableMapOf())

    init {
        val defaultConfiguration = datasources["primary"]
        if (defaultConfiguration != null) {
            datasources[GeneratorExtension.DEFAULT_MODULE_NAME] = defaultConfiguration
        } else {
            try {
                if (dataSourceProperties != null) {
                    val configuration = DatabaseConfiguration()
                    configuration.url = dataSourceProperties.determineUrl() ?: ""
                    configuration.username = dataSourceProperties.determineUsername() ?: ""
                    configuration.password = dataSourceProperties.determinePassword() ?: ""
                    configuration.driverClass =
                            dataSourceProperties.determineDriverClassName() ?: ""

                    datasources[GeneratorExtension.DEFAULT_MODULE_NAME] = configuration
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

            for (key in datasources.keys) {
                writer.println("<a href=\"$basePath/$key\">$key.puml</a>")
            }
            writer.println("</pre><hr></body>\n</html>")
        }
    }

    @ReadOperation
    fun puml(@Selector module: String) {
        val database = datasources[module]
                ?: throw IllegalArgumentException("module $module not found")
        val tables = database.tables()
        val tmpPath = System.getProperty("java.io.tmpdir")
        val destFile = File(tmpPath, "summer/puml/${module}.puml")
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
        response.setDateHeader("Expires", 0)

        response.outputStream.buffered().use { bos ->
            bos.write(destFile.inputStream().readBytes())
        }
    }


}
