package top.bettercode.summer.tools.generator

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

class LinkedProperties(
    private val omitComments: Boolean = false,
    private val map: LinkedHashMap<Any?, Any?> = linkedMapOf()
) : Properties() {

    companion object {
        @JvmField
        val EOL: String = System.lineSeparator()
    }

    override fun store(out: OutputStream, comments: String?) {
        val baos = ByteArrayOutputStream()
        super.store(baos, if (omitComments) null else comments)
        val contents = baos.toString(StandardCharsets.ISO_8859_1.name())
        for (line in contents.split(EOL)) {
            if (!(omitComments && line.startsWith("#"))) {
                out.write((line + EOL).toByteArray(StandardCharsets.ISO_8859_1))
            }
        }
    }


    override fun store(writer: Writer, comments: String?) {
        val stringWriter = StringWriter()
        super.store(stringWriter, if (omitComments) null else comments)
        val contents = stringWriter.toString()
        for (line in contents.split(EOL)) {
            if (!(omitComments && line.startsWith("#"))) {
                writer.write(line + EOL)
            }
        }
    }


    override fun storeToXML(out: OutputStream, comments: String?) {
        super.storeToXML(out, if (omitComments) null else comments)
    }


    override fun storeToXML(out: OutputStream, comments: String?, encoding: String) {
        super.storeToXML(out, if (omitComments) null else comments, encoding)
    }

    //--------------------------------------------

    override fun put(key: Any?, value: Any?): Any? {
        return map.put(key, value)
    }

    override val entries: MutableSet<MutableMap.MutableEntry<Any?, Any?>>
        get() = map.entries


    override fun getProperty(key: String?): String? {
        return map[key]?.toString()
    }

    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun keys(): Enumeration<Any?> {
        return Collections.enumeration(map.keys)
    }

    override fun elements(): Enumeration<Any> {
        return Collections.enumeration(map.values)
    }

    override fun contains(value: Any?): Boolean {
        return map.contains(value)
    }

    override fun containsKey(key: Any?): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: Any?): Boolean {
        return map.containsValue(value)
    }

    override fun get(key: Any?): Any? {
        return map[key]
    }

    override fun remove(key: Any?): Any? {
        return map.remove(key)
    }

    override fun putAll(from: Map<*, *>) {
        map.putAll(from)
    }

    override fun clear() {
        map.clear()
    }

    override fun toString(): String {
        return map.toString()
    }

    override val keys: MutableSet<Any?>
        get() = map.keys

    override val values: MutableCollection<Any?>
        get() = map.values

    override fun equals(other: Any?): Boolean {
        return map == other
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun getOrDefault(key: Any?, defaultValue: Any?): Any? {
        return map.getOrDefault(key, defaultValue)
    }

    override fun remove(key: Any?, value: Any?): Boolean {
        return map.remove(key, value)
    }

    override fun forEach(action: BiConsumer<in Any?, in Any?>) {
        map.forEach(action)
    }

    override fun replaceAll(function: BiFunction<in Any?, in Any?, *>) {
        map.replaceAll(function)
    }

    override fun putIfAbsent(key: Any?, value: Any?): Any? {
        return map.putIfAbsent(key, value)
    }

    override fun replace(key: Any?, oldValue: Any?, newValue: Any?): Boolean {
        return map.replace(key, oldValue, newValue)
    }

    override fun replace(key: Any?, value: Any?): Any? {
        return map.replace(key, value)
    }

    override fun computeIfAbsent(key: Any?, mappingFunction: Function<in Any?, *>): Any? {
        return map.computeIfAbsent(key, mappingFunction)
    }

    override fun computeIfPresent(
        key: Any?,
        remappingFunction: BiFunction<in Any?, in Any, *>
    ): Any? {
        return map.computeIfPresent(key, remappingFunction)
    }

    override fun compute(key: Any?, remappingFunction: BiFunction<in Any?, in Any?, *>): Any? {
        return map.compute(key, remappingFunction)
    }

    override fun merge(
        key: Any?,
        value: Any,
        remappingFunction: BiFunction<in Any, in Any, *>
    ): Any? {
        return map.merge(key, value, remappingFunction)
    }

    override fun clone(): Any {
        return map.clone()
    }


}