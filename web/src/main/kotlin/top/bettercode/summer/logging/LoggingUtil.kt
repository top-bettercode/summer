package top.bettercode.summer.logging

import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment
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


    val apiAddress: Pair<String, Pair<String,Int>> by lazy {
        ApplicationContextHolder.environment?.let {
            apiAddress(it, RequestConverter.SCHEME_HTTP)
        } ?: throw RuntimeException("environment 未初始化")
    }

    val apiAddressWs: Pair<String, Pair<String,Int>> by lazy {
        ApplicationContextHolder.environment?.let {
            apiAddress(it, "ws")
        } ?: throw RuntimeException("environment 未初始化")
    }


    fun apiAddress(
        environment: Environment,
        scheme: String = RequestConverter.SCHEME_HTTP
    ): Pair<String, Pair<String,Int>> {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)

        val serverProperties = Binder.get(environment).bind(
            "server", ServerProperties::class.java
        ).get()
        val serverPort = serverProperties.port ?: 8080
        val host = IPAddressUtil.inet4Address
        printer.printf("%s://%s", scheme, host)
        if (serverPort != RequestConverter.STANDARD_PORT_HTTP) {
            printer.printf(":%d", serverPort)
        }
        val contextPath = serverProperties.servlet?.contextPath ?: "/"
        if ("/" != contextPath)
            printer.print(contextPath)
        return uriWriter.toString() to  (host to serverPort)
    }

    fun actuatorAddress(environment: Environment): Pair<String, Pair<String,Int>>
    {
        val uriWriter = StringWriter()
        val printer = PrintWriter(uriWriter)
        val managementServerProperties = Binder.get(environment).bind(
            "management.server", ManagementServerProperties::class.java
        ).orElse(ManagementServerProperties())

        if (managementServerProperties.port == null) {
            return apiAddress(environment)
        }
        val host = (managementServerProperties.address?.hostName ?: IPAddressUtil.inet4Address)
        val serverPort = managementServerProperties.port!!
        printer.printf("%s://%s", RequestConverter.SCHEME_HTTP, host)
        if (serverPort != RequestConverter.STANDARD_PORT_HTTP) {
            printer.printf(":%d", serverPort)
        }
        val contextPath = managementServerProperties.basePath ?: ""
        if (contextPath.isNotBlank() && "/" != contextPath)
            printer.print(contextPath)

        return uriWriter.toString() to  (host to serverPort)
    }

    internal fun warnTitle(environment: Environment): String = environment.getProperty(
        "summer.logging.warn-title",
        "${environment.getProperty("spring.application.name", "").replace(Regex(" +"), "-")}${
            if (existProperty(environment, "summer.web.project-name"))
                " ${
                    environment.getProperty("summer.web.project-name", "").replace(Regex(" +"), "-")
                }"
            else
                ""
        } ${environment.getProperty("spring.profiles.active", "")}"
    )

    internal fun existProperty(environment: Environment, key: String) =
        environment.containsProperty(key) && !environment.getProperty(key).isNullOrBlank()

}