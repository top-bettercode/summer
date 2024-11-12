package top.bettercode.summer.logging.async

import org.slf4j.event.Level

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Loggable(val value: Level = Level.INFO)
