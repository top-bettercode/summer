package top.bettercode.summer.security.support

interface SecurityParameterNames {
    companion object {
        /**
         * `grant_type` - used in Access Token Request.
         */
        const val GRANT_TYPE = "grant_type"

        /**
         * `scope` - used in Authorization Request, Authorization Response, Access Token Request and
         * Access Token Response.
         */
        const val SCOPE = "scope"

        /**
         * `access_token` - used in Authorization Response and Access Token Response.
         */
        const val ACCESS_TOKEN = "access_token"

        /**
         * 兼容toekn名称
         */
        const val COMPATIBLE_ACCESS_TOKEN = "accessToken"

        /**
         * `refresh_token` - used in Access Token Request and Access Token Response.
         */
        const val REFRESH_TOKEN = "refresh_token"

        /**
         * `username` - used in Access Token Request.
         */
        const val USERNAME = "username"

        /**
         * `password` - used in Access Token Request.
         */
        const val PWDNAME = "password"

        /**
         * revoke token
         */
        const val REVOKE_TOKEN = "revoke_token"
    }
}
