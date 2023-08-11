package top.bettercode.summer.tools.lang.util

import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

enum class OS {
    /**
     * IBM AIX operating system.
     *
     * @since 5.3
     */
    AIX,

    /**
     * Linux-based operating system.
     */
    LINUX,

    /**
     * Apple Macintosh operating system (e.g., macOS).
     */
    MAC,

    /**
     * Oracle Solaris operating system.
     */
    SOLARIS,

    /**
     * Microsoft Windows operating system.
     */
    WINDOWS,

    /**
     * An operating system other than [.AIX], [.LINUX], [.MAC], [.SOLARIS], or
     * [.WINDOWS].
     */
    OTHER,

    /**
     * Unknown operating system.
     */
    UNKNOWN;

    /**
     * @return `true` if *this* `OS` is known to be the operating system on which
     * the current JVM is executing
     */
    val isCurrentOs: Boolean
        get() = this == CURRENT_OS

    companion object {
        private val logger = LoggerFactory.getLogger(OS::class.java)

        @JvmField
        val CURRENT_OS = determineCurrentOs()

        private fun determineCurrentOs(): OS {
            return parse(System.getProperty("os.name"))
        }

        fun parse(osName: String): OS {
            var name = osName
            if (!StringUtils.hasText(name)) {
                logger.debug(
                        "JVM system property 'os.name' is undefined. It is therefore not possible to detect the current OS."
                )

                // null signals that the current OS is "unknown"
                return UNKNOWN
            }
            name = name.lowercase()
            if (name.contains("aix")) {
                return AIX
            }
            if (name.contains("linux")) {
                return LINUX
            }
            if (name.contains("mac")) {
                return MAC
            }
            if (name.contains("sunos") || name.contains("solaris")) {
                return SOLARIS
            }
            return if (name.contains("win")) {
                WINDOWS
            } else OTHER
        }
    }
}