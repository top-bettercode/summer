package top.bettercode.summer.logging.async

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.cglib.proxy.MethodProxy
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import top.bettercode.summer.tools.lang.operation.HttpOperation
import java.lang.reflect.Method
import kotlin.random.Random

class LoggerAnnotationBeanPostProcessor : BeanPostProcessor {
    private val logger: Logger = LoggerFactory.getLogger(
        LoggerAnnotationBeanPostProcessor::class.java
    )

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val targetClass: Class<*> = bean.javaClass
        val methods = targetClass.methods

        var needProxy = false
        for (method in methods) {
            if ((AnnotationUtils.findAnnotation(
                    method,
                    Async::class.java
                ) != null || AnnotationUtils.findAnnotation(
                    method,
                    Scheduled::class.java
                ) != null)
            ) {
                needProxy = true
                break
            }
        }

        if (needProxy) {
            return Enhancer.create(
                targetClass.superclass,
                MethodInterceptor { obj: Any?, method: Method, args: Array<Any?>?, proxy: MethodProxy ->
                    if ((AnnotationUtils.findAnnotation(method, Async::class.java) != null
                                || AnnotationUtils.findAnnotation(
                            method, Scheduled::class.java
                        ) != null)
                    ) {
                        try {
                            val traceid = Integer.toHexString(Random.nextInt())
                            MDC.put(HttpOperation.MDC_TRACEID, traceid)
                            logger.info("==={} started===", method.toString())
                            val result = proxy.invokeSuper(obj, args)
                            logger.info("==={} finished===", method.toString())
                            return@MethodInterceptor result
                        } catch (e: Throwable) {
                            logger.error("==={} error===", method.toString(), e)
                            throw e
                        } finally {
                            MDC.remove(HttpOperation.MDC_TRACEID)
                        }
                    } else {
                        return@MethodInterceptor proxy.invokeSuper(obj, args)
                    }
                })
        } else
            return bean
    }

}
