package top.bettercode.summer.tools.lang.util

import java.time.LocalDateTime

/**
 * @author Peter Wu
 */
@Deprecated(
    "use top.bettercode.summer.tools.lang.util.TimeUtil",
    ReplaceWith("top.bettercode.summer.tools.lang.util.TimeUtil")
)
class LocalDateTimeHelper private constructor(
    localDateTime: LocalDateTime
) : TimeUtil(localDateTime)
