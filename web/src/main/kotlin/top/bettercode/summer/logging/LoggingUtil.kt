package top.bettercode.summer.logging

import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.core.env.Environment
import org.springframework.util.Assert
import top.bettercode.summer.tools.lang.operation.RequestConverter
import top.bettercode.summer.tools.lang.util.IPAddressUtil
import top.bettercode.summer.web.support.ApplicationContextHolder
import java.io.PrintWriter
import java.io.StringWriter

/**
 *
 * @author Peter Wu
 */
object LoggingUtil {

    val apiAddress: String by lazy {
        apiAddress(RequestConverter.SCHEME_HTTP)
    }

    val apiAddressWs: String by lazy {
        apiAddress("ws")
    }

    fun apiAddress(scheme: String = RequestConverter.SCHEME_HTTP): String {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)
        val serverProperties = ApplicationContextHolder.getBean(ServerProperties::class.java)
        Assert.notNull(serverProperties, "serverProperties must not be null")
        val serverPort = serverProperties!!.port ?: 8080
        printer.printf("%s://%s", scheme, IPAddressUtil.inet4Address)
        if (serverPort != RequestConverter.STANDARD_PORT_HTTP) {
            printer.printf(":%d", serverPort)
        }
        val contextPath = serverProperties.servlet?.contextPath ?: "/"
        if ("/" != contextPath)
            printer.print(contextPath)
        return uriWriter.toString()
    }

    val actuatorAddress: String by lazy {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)
        val properties = ApplicationContextHolder.getBean(ManagementServerProperties::class.java)
        Assert.notNull(properties, "ManagementServerProperties must not be null")
        if (properties!!.address == null) {
            return@lazy apiAddress
        }
        val serverPort = properties.port!!
        printer.printf("%s://%s", RequestConverter.SCHEME_HTTP, properties.address)
        if (serverPort != RequestConverter.STANDARD_PORT_HTTP) {
            printer.printf(":%d", serverPort)
        }
        val contextPath = properties.basePath ?: ""
        if (contextPath.isNotBlank() && "/" != contextPath)
            printer.print(contextPath)

        uriWriter.toString()
    }

    internal fun warnSubject(environment: Environment): String = environment.getProperty(
            "summer.logging.warn-subject",
            "${environment.getProperty("spring.application.name", "")}${
                if (existProperty(
                                environment,
                                "summer.web.project-name"
                        )
                ) " " + environment.getProperty("summer.web.project-name") else ""
            } ${environment.getProperty("spring.profiles.active", "")}"
    )

    internal fun existProperty(environment: Environment, key: String) =
            environment.containsProperty(key) && !environment.getProperty(key).isNullOrBlank()

}