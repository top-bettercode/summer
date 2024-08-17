package top.bettercode.summer.web

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import org.springframework.util.Assert
import java.util.*

class PagedResources<T>(number: Int, size: Int, totalPages: Int, totalElements: Long, content: T) {
    @get:JsonView(Any::class)
    var content: T? = content

    @get:JsonView(Any::class)
    var page: PageMetadata? = null

    init {
        this.page = PageMetadata(number, size, totalPages, totalElements)
    }

    class PageMetadata(
        @JsonView(Any::class)
        @JsonProperty var number: Int, @JsonView(Any::class)
        @JsonProperty var size: Int, @JsonView(Any::class)
        @JsonProperty var totalPages: Int, @JsonView(Any::class)
        @JsonProperty var totalElements: Long
    ) {

        init {
            Assert.isTrue(number > -1, "Number must not be negative!")
            Assert.isTrue(size > -1, "Size must not be negative!")
            Assert.isTrue(totalElements > -1, "Total elements must not be negative!")
            Assert.isTrue(totalPages > -1, "Total pages must not be negative!")
        }

        override fun toString(): String {
            return String.format(
                "Metadata { number: %d, total pages: %d, total elements: %d, size: %d }", number,
                totalPages, totalElements, size
            )
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