package top.bettercode.summer.logging

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 *
 * @author Peter Wu
 */
@Component
class AsyncService {

    @Async
    fun async() {
        System.err.println("async")
    }

    @Scheduled(fixedRate = 1000)
    fun scheduled() {
        System.err.println("scheduled")
    }

}