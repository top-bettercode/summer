package top.bettercode.lang.util

import java.time.LocalDateTime

/**
 * @author Peter Wu
 */
@Deprecated(
    "use top.bettercode.lang.util.TimeUtil",
    ReplaceWith("top.bettercode.lang.util.TimeUtil")
)
class LocalDateTimeHelper private constructor(
    localDateTime: LocalDateTime
) : TimeUtil(localDateTime)
