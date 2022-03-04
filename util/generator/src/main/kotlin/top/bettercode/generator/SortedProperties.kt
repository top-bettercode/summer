package top.bettercode.generator

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Specialization of Properties that sorts properties alphanumerically
 * based on their keys.
 *
 *
 * This can be useful when storing the Properties instance in a
 * properties file, since it allows such files to be generated in a repeatable
 * manner with consistent ordering of properties.
 *
 *
 * Comments in generated properties files can also be optionally omitted.
 *
 */
class SortedProperties
/**
 * Construct a new `SortedProperties` instance that honors the supplied
 * `omitComments` flag.
 * @param omitComments `true` if comments should be omitted when
 * storing properties in a file
 */(private val omitComments: Boolean = false) : Properties() {
    /**
     * Construct a new `SortedProperties` instance with properties populated
     * from the supplied Properties object and honoring the supplied
     * `omitComments` flag.
     *
     * Default properties from the supplied `Properties` object will
     * not be copied.
     * @param properties the `Properties` object from which to copy the
     * initial properties
     * @param omitComments `true` if comments should be omitted when
     * storing properties in a file
     */
    constructor(properties: Properties?, omitComments: Boolean) : this(omitComments) {
        putAll(properties!!)
    }

    override fun store(out: OutputStream, comments: String?) {
        val baos = ByteArrayOutputStream()
        super.store(baos, if (omitComments) null else comments)
        val contents = baos.toString(StandardCharsets.ISO_8859_1.name())
        for (line in contents.split(EOL).toTypedArray()) {
            if (!(omitComments && line.startsWith("#"))) {
                out.write((line + EOL).toByteArray(StandardCharsets.ISO_8859_1))
            }
        }
    }


    override fun store(writer: Writer, comments: String?) {
        val stringWriter = StringWriter()
        super.store(stringWriter, if (omitComments) null else comments)
        val contents = stringWriter.toString()
        for (line in contents.split(EOL).toTypedArray()) {
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

    /**
     * Return a sorted enumeration of the keys in this Properties object.
     * @see .keySet
     */
    @Synchronized
    override fun keys(): Enumeration<Any> {
        return Collections.enumeration(keys)
    }

    /**
     * Return a sorted set of the keys in this Properties object.
     *
     * The keys will be converted to strings if necessary using
     * String.valueOf and sorted alphanumerically according to
     * the natural order of strings.
     */
    override val keys: MutableSet<Any>
        get() {
            val sortedKeys: MutableSet<Any> = TreeSet(keyComparator)
            sortedKeys.addAll(super.keys)
            return Collections.synchronizedSet(sortedKeys)
        }

    /**
     * Return a sorted set of the entries in this Properties object.
     *
     * The entries will be sorted based on their keys, and the keys will be
     * converted to strings if necessary using String.valueOf
     * and compared alphanumerically according to the natural order of strings.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
        get() {
            val sortedEntries: MutableSet<MutableMap.MutableEntry<Any, Any>> =
                TreeSet(entryComparator)
            sortedEntries.addAll(super.entries)
            return Collections.synchronizedSet(sortedEntries)
        }

    companion object {
        private const val serialVersionUID: Long = 1L

        @JvmField
        val EOL = System.lineSeparator()
        private val keyComparator =
            Comparator.comparing<Any, String> { obj: Any? -> java.lang.String.valueOf(obj) }
        private val entryComparator = java.util.Map.Entry.comparingByKey<Any, Any>(keyComparator)
    }
}