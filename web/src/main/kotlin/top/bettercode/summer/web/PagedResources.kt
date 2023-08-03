package top.bettercode.summer.web

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.util.Assert
import java.util.*
import kotlin.math.ceil

class PagedResources<T> {
    @get:JsonView(Any::class)
    var content: T? = null
        private set

    @get:JsonView(Any::class)
    var page: PageMetadata? = null

    constructor()
    constructor(metadata: PageMetadata?, content: T) {
        this.content = content
        page = metadata
    }

    fun setContent(content: T) {
        this.content = content
    }

    class PageMetadata {
        @JsonView(Any::class)
        @JsonProperty
        var number: Long = 0

        @JsonView(Any::class)
        @JsonProperty
        var size: Long = 0

        @JsonView(Any::class)
        @JsonProperty
        var totalPages: Long = 0

        @JsonView(Any::class)
        @JsonProperty
        var totalElements: Long = 0

        constructor()
        constructor(number: Long, size: Long, totalPages: Long, totalElements: Long) {
            Assert.isTrue(number > -1, "Number must not be negative!")
            Assert.isTrue(size > -1, "Size must not be negative!")
            Assert.isTrue(totalElements > -1, "Total elements must not be negative!")
            Assert.isTrue(totalPages > -1, "Total pages must not be negative!")
            this.number = number
            this.size = size
            this.totalPages = totalPages
            this.totalElements = totalElements
        }

        constructor(number: Long, size: Long, totalElements: Long) : this(number, size, if (size == 0L) 0 else ceil(totalElements.toDouble() / size.toDouble()).toLong(),
                totalElements)

        override fun toString(): String {
            return String.format("Metadata { number: %d, total pages: %d, total elements: %d, size: %d }", number,
                    totalPages, totalElements, size)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is PageMetadata) {
                return false
            }
            return number == other.number && size == other.size && totalPages == other.totalPages && totalElements == other.totalElements
        }

        override fun hashCode(): Int {
            return Objects.hash(number, size, totalPages, totalElements)
        }
    }
}