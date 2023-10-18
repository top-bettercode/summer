package top.bettercode.summer.tools.lang.util

/**
 * **NOTE:** This class was copied from plexus-utils, to allow this library
 * to stand completely self-contained.
 * <br></br>
 * Condition that tests the OS type.
 *
 */
class Os {
    private var family: String? = null
    private var name: String? = null
    private var version: String? = null
    private var arch: String? = null

    /**
     * Default constructor
     */
    constructor()

    /**
     * Constructor that sets the family attribute
     *
     * @param family a String value
     */
    constructor(family: String) {
        setFamily(family)
    }

    /**
     * Sets the desired OS family type
     *
     * @param f The OS family type desired<br></br>
     * Possible values:<br></br>
     *
     *  * dos
     *  * mac
     *  * netware
     *  * os/2
     *  * tandem
     *  * unix
     *  * windows
     *  * win9x
     *  * z/os
     *  * os/400
     *  * openvms
     *
     */
    fun setFamily(f: String) {
        family = f.lowercase()
    }

    /**
     * Sets the desired OS name
     *
     * @param name The OS name
     */
    fun setName(name: String) {
        this.name = name.lowercase()
    }

    /**
     * Sets the desired OS architecture
     *
     * @param arch The OS architecture
     */
    fun setArch(arch: String) {
        this.arch = arch.lowercase()
    }

    /**
     * Sets the desired OS version
     *
     * @param version The OS version
     */
    fun setVersion(version: String) {
        this.version = version.lowercase()
    }

    /**
     * Determines if the current OS matches the type of that
     * set in setFamily.
     *
     * @see Os.setFamily
     */
    @Throws(Exception::class)
    fun eval(): Boolean {
        return isOs(family, name, arch, version)
    }

    companion object {
        // define the families for easier reference
        const val FAMILY_DOS = "dos"
        const val FAMILY_MAC = "mac"
        const val FAMILY_NETWARE = "netware"
        const val FAMILY_OS2 = "os/2"
        const val FAMILY_TANDEM = "tandem"
        const val FAMILY_UNIX = "unix"
        const val FAMILY_WINDOWS = "windows"
        const val FAMILY_WIN9X = "win9x"
        const val FAMILY_ZOS = "z/os"
        const val FAMILY_OS400 = "os/400"
        const val FAMILY_OPENVMS = "openvms"

        // store the valid families
        private val validFamilies = setValidFamilies()

        // get the current info
        private val PATH_SEP = System.getProperty("path.separator")
        val OS_NAME = System.getProperty("os.name").lowercase()
        val OS_ARCH = System.getProperty("os.arch").lowercase()
        val OS_VERSION = System.getProperty("os.version").lowercase()

        // Make sure this method is called after static fields it depends on have been set!
        val OS_FAMILY = osFamily

        /**
         * Initializes the set of valid families.
         */
        private fun setValidFamilies(): Set<String> {
            val valid: MutableSet<String> = HashSet()
            valid.add(FAMILY_DOS)
            valid.add(FAMILY_MAC)
            valid.add(FAMILY_NETWARE)
            valid.add(FAMILY_OS2)
            valid.add(FAMILY_TANDEM)
            valid.add(FAMILY_UNIX)
            valid.add(FAMILY_WINDOWS)
            valid.add(FAMILY_WIN9X)
            valid.add(FAMILY_ZOS)
            valid.add(FAMILY_OS400)
            valid.add(FAMILY_OPENVMS)
            return valid
        }

        /**
         * Determines if the current OS matches the given OS
         * family.
         *
         * @param family the family to check for
         * @return true if the OS matches
         * @since 1.0
         */
        fun isFamily(family: String?): Boolean {
            return isOs(family, null, null, null)
        }

        /**
         * Determines if the current OS matches the given OS
         * name.
         *
         * @param name the OS name to check for
         * @return true if the OS matches
         * @since 1.0
         */
        fun isName(name: String?): Boolean {
            return isOs(null, name, null, null)
        }

        /**
         * Determines if the current OS matches the given OS
         * architecture.
         *
         * @param arch the OS architecture to check for
         * @return true if the OS matches
         * @since 1.0
         */
        fun isArch(arch: String?): Boolean {
            return isOs(null, null, arch, null)
        }

        /**
         * Determines if the current OS matches the given OS
         * version.
         *
         * @param version the OS version to check for
         * @return true if the OS matches
         * @since 1.0
         */
        fun isVersion(version: String?): Boolean {
            return isOs(null, null, null, version)
        }

        /**
         * Determines if the current OS matches the given OS
         * family, name, architecture and version.
         *
         * The name, archictecture and version are compared to
         * the System properties os.name, os.version and os.arch
         * in a case-independent way.
         *
         * @param family The OS family
         * @param name The OS name
         * @param arch The OS architecture
         * @param version The OS version
         * @return true if the OS matches
         * @since 1.0
         */
        fun isOs(family: String?, name: String?, arch: String?, version: String?): Boolean {
            var retValue = false
            if (family != null || name != null || arch != null || version != null) {
                var isFamily = true
                var isName = true
                var isArch = true
                var isVersion = true
                if (family != null) {
                    isFamily = if (family.equals(FAMILY_WINDOWS, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_WINDOWS)
                    } else if (family.equals(FAMILY_OS2, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_OS2)
                    } else if (family.equals(FAMILY_NETWARE, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_NETWARE)
                    } else if (family.equals(FAMILY_DOS, ignoreCase = true)) {
                        PATH_SEP == ";" && !isFamily(FAMILY_NETWARE)
                    } else if (family.equals(FAMILY_MAC, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_MAC)
                    } else if (family.equals(FAMILY_TANDEM, ignoreCase = true)) {
                        OS_NAME.contains("nonstop_kernel")
                    } else if (family.equals(FAMILY_UNIX, ignoreCase = true)) {
                        (PATH_SEP == ":" && !isFamily(FAMILY_OPENVMS)
                                && (!isFamily(FAMILY_MAC) || OS_NAME.endsWith("x")))
                    } else if (family.equals(FAMILY_WIN9X, ignoreCase = true)) {
                        (isFamily(FAMILY_WINDOWS)
                                && (OS_NAME.contains("95") || OS_NAME.contains("98")
                                || OS_NAME.contains("me") || OS_NAME.contains("ce")))
                    } else if (family.equals(FAMILY_ZOS, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_ZOS) || OS_NAME.contains("os/390")
                    } else if (family.equals(FAMILY_OS400, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_OS400)
                    } else if (family.equals(FAMILY_OPENVMS, ignoreCase = true)) {
                        OS_NAME.contains(FAMILY_OPENVMS)
                    } else {
                        OS_NAME.contains(family.lowercase())
                    }
                }
                if (name != null) {
                    isName = name.lowercase() == OS_NAME
                }
                if (arch != null) {
                    isArch = arch.lowercase() == OS_ARCH
                }
                if (version != null) {
                    isVersion = version.lowercase() == OS_VERSION
                }
                retValue = isFamily && isName && isArch && isVersion
            }
            return retValue
        }

        private val osFamily: String?
            /**
             * Helper method to determine the current OS family.
             *
             * @return name of current OS family.
             * @since 1.4.2
             */
            get() {
                // in case the order of static initialization is
                // wrong, get the list
                // safely.
                val families: Set<String> = validFamilies.ifEmpty {
                    setValidFamilies()
                }
                for (fam in families) {
                    if (isFamily(fam)) {
                        return fam
                    }
                }
                return null
            }

        /**
         * Helper method to check if the given family is in the
         * following list:
         *
         *  * dos
         *  * mac
         *  * netware
         *  * os/2
         *  * tandem
         *  * unix
         *  * windows
         *  * win9x
         *  * z/os
         *  * os/400
         *  * openvms
         *
         *
         * @param theFamily the family to check.
         * @return true if one of the valid families.
         * @since 1.4.2
         */
        fun isValidFamily(theFamily: String): Boolean {
            return validFamilies.contains(theFamily)
        }

        /**
         * @return a copy of the valid families
         * @since 1.4.2
         */
        fun getValidFamilies(): Set<String> {
            return HashSet(validFamilies)
        }
    }
}
