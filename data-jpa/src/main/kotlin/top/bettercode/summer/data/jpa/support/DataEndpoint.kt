package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.lang.Nullable


/**
 * @author Peter Wu
 */
@Endpoint(id = "data")
open class DataEndpoint(
    private val dataQuery: DataQuery
) {
    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")

    @WriteOperation
    open fun write(
        @Selector ds: String, @Selector op: String, sql: String,
        @Nullable page: Int?,
        @Nullable size: Int?
    ): Any {
        return when (op) {
            "update" -> {
                dataQuery.update(ds, sql)
            }

            "query" -> {
                dataQuery.query(ds, sql, page, size)
            }

            else -> {
                ""
            }
        }
    }

}