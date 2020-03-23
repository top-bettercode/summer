package cn.bestwu.lang.util

import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * 文件工具类
 *
 * @author Peter Wu
 */
object FileUtil {

    private val log = LoggerFactory.getLogger(FileUtil::class.java)

    //-----------------------------------------------------------------------
    @JvmStatic
    fun toCharset(charset: Charset?): Charset {
        return charset ?: Charset.defaultCharset()
    }

    @JvmStatic
    fun toCharset(charset: String?): Charset {
        return if (charset == null) Charset.defaultCharset() else Charset.forName(charset)
    }

    //-----------------------------------------------------------------------

    /**
     * @param file file
     * @return FileInputStream
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun openInputStream(file: File): FileInputStream {
        if (file.exists()) {
            if (file.isDirectory) {
                throw IOException("File '$file' exists but is a directory")
            }
            if (!file.canRead()) {
                throw IOException("File '$file' cannot be read")
            }
        } else {
            throw FileNotFoundException("File '$file' does not exist")
        }
        return FileInputStream(file)
    }

    //-----------------------------------------------------------------------

    @Throws(IOException::class)
    @JvmStatic
    fun openOutputStream(file: File, append: Boolean): FileOutputStream {
        if (file.exists()) {
            if (file.isDirectory) {
                throw IOException("File '$file' exists but is a directory")
            }
            if (!file.canWrite()) {
                throw IOException("File '$file' cannot be written to")
            }
        } else {
            val parent = file.parentFile
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory) {
                    throw IOException("Directory '$parent' could not be created")
                }
            }
        }
        return FileOutputStream(file, append)
    }

    //-----------------------------------------------------------------------

    @JvmStatic
    fun listFiles(directory: File, filter: FileFilter, recursive: Boolean): Collection<File> {
        val files = java.util.LinkedList<File>()
        innerListFiles(files, directory, filter, false, recursive)
        return files
    }

    private fun innerListFiles(files: MutableCollection<File>, directory: File, filter: FileFilter,
                               includeSubDirectories: Boolean, recursive: Boolean) {
        val found = directory.listFiles(filter)

        if (found != null) {
            for (file in found) {
                if (file.isDirectory) {
                    if (includeSubDirectories) {
                        files.add(file)
                    }
                    if (recursive) {
                        innerListFiles(files, file, filter, includeSubDirectories, recursive)
                    }
                } else {
                    files.add(file)
                }
            }
        }
    }

    //-----------------------------------------------------------------------

    @Throws(IOException::class)
    @JvmStatic
    fun readLines(input: InputStream, encoding: Charset): List<String> {
        val reader = BufferedReader(InputStreamReader(input, toCharset(encoding)))
        val list = ArrayList<String>()
        var line: String? = reader.readLine()
        while (line != null) {
            list.add(line)
            line = reader.readLine()
        }
        return list
    }

    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun readLines(file: File, encoding: Charset = Charset.defaultCharset()): List<String> {
        var `in`: InputStream? = null
        try {
            `in` = openInputStream(file)
            return readLines(`in`, toCharset(encoding))
        } finally {
            try {
                `in`?.close()
            } catch (ioe: IOException) {
                // ignore
            }

        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun readLines(file: File, encoding: String): List<String> {
        return readLines(file, toCharset(encoding))
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(file: File, encoding: String, lines: Collection<*>, append: Boolean) {
        writeLines(file, encoding, lines, null, append)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(file: File, lines: Collection<*>) {
        writeLines(file, null, lines, null, false)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(file: File, lines: Collection<*>, append: Boolean) {
        writeLines(file, null, lines, null, append)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(lines: Collection<*>?, lineEnding: String?, output: OutputStream,
                   encoding: Charset) {
        var ending = lineEnding
        if (lines == null) {
            return
        }
        if (ending == null) {
            ending = System.getProperty("line.separator")
        }
        val cs = toCharset(encoding)
        for (line in lines) {
            if (line != null) {
                output.write(line.toString().toByteArray(cs))
            }
            output.write(ending!!.toByteArray(cs))
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    @JvmStatic
    fun writeLines(file: File, encoding: String?, lines: Collection<*>, lineEnding: String? = null,
                   append: Boolean = false) {
        var out: FileOutputStream? = null
        try {
            out = openOutputStream(file, append)
            val buffer = BufferedOutputStream(out)
            writeLines(lines, lineEnding, buffer, toCharset(encoding))
            buffer.flush()
            out.close() // don't swallow close Exception if copy completes normally
        } finally {
            try {
                out?.close()
            } catch (ioe: IOException) {
                // ignore
            }

        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(file: File, lines: Collection<*>, lineEnding: String) {
        writeLines(file, null, lines, lineEnding, false)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun writeLines(file: File, lines: Collection<*>, lineEnding: String, append: Boolean) {
        writeLines(file, null, lines, lineEnding, append)
    }

    //-----------------------------------------------------------------------

    /**
     * 删除文件
     *
     * @param file 文件
     * @return 是否成功
     */
    @JvmStatic
    fun delete(file: File): Boolean {
        if (!file.exists()) {
            return false
        }

        if (file.isDirectory) {
            log.error("无权删除文件夹:{}", file)
            return false
        }

        val delete = file.delete()
        if (log.isDebugEnabled && delete) {
            log.debug("删除文件：{}", file)
        }
        return delete
    }
}//-----------------------------------------------------------------------