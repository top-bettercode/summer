package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StreetNumber(

        @field:JsonProperty("number")
        val number: Array<String>? = null,

        @field:JsonProperty("location")
        val location: Array<String>? = null,

        @field:JsonProperty("direction")
        val direction: Array<String>? = null,

        @field:JsonProperty("distance")
        val distance: Array<String>? = null,

        @field:JsonProperty("street")
        val street: Array<String>? = null
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is StreetNumber) return false

                if (number != null) {
                        if (other.number == null) return false
                        if (!number.contentEquals(other.number)) return false
                } else if (other.number != null) return false
                if (location != null) {
                        if (other.location == null) return false
                        if (!location.contentEquals(other.location)) return false
                } else if (other.location != null) return false
                if (direction != null) {
                        if (other.direction == null) return false
                        if (!direction.contentEquals(other.direction)) return false
                } else if (other.direction != null) return false
                if (distance != null) {
                        if (other.distance == null) return false
                        if (!distance.contentEquals(other.distance)) return false
                } else if (other.distance != null) return false
                if (street != null) {
                        if (other.street == null) return false
                        if (!street.contentEquals(other.street)) return false
                } else if (other.street != null) return false

                return true
        }

        override fun hashCode(): Int {
                var result = number?.contentHashCode() ?: 0
                result = 31 * result + (location?.contentHashCode() ?: 0)
                result = 31 * result + (direction?.contentHashCode() ?: 0)
                result = 31 * result + (distance?.contentHashCode() ?: 0)
                result = 31 * result + (street?.contentHashCode() ?: 0)
                return result
        }
}