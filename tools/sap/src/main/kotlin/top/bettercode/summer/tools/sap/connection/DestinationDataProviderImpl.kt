package top.bettercode.summer.tools.sap.connection

import com.sap.conn.jco.ext.DestinationDataEventListener
import com.sap.conn.jco.ext.DestinationDataProvider
import org.springframework.util.StringUtils
import java.util.*

class DestinationDataProviderImpl : DestinationDataProvider {
    private val provider: MutableMap<String, Properties> = HashMap()
    fun addDestinationProperties(destName: String, props: Properties) {
        provider[destName] = props
    }

    override fun getDestinationProperties(destName: String): Properties {
        if (!StringUtils.hasText(destName)) {
            throw NullPointerException("Destinantion name is empty.")
        }
        check(provider.isNotEmpty()) { "Data provider is empty." }
        return provider[destName]!!
    }

    override fun setDestinationDataEventListener(listener: DestinationDataEventListener) {
        throw UnsupportedOperationException()
    }

    override fun supportsEvents(): Boolean {
        return false
    }
}