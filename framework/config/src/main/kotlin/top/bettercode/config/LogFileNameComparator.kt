package top.bettercode.config

import java.io.File

/**
 *
 * @author Peter Wu
 */
class LogFileNameComparator : Comparator<File> {

    override fun compare(o1: File, o2: File): Int {
        val compareTo = o1.isFile.compareTo(o2.isFile)
        return if (compareTo == 0) {
            if (o1.isDirectory) {
                o1.name.compareTo(o2.name)
            } else {
                try {
                    val name1 = o1.nameWithoutExtension
                    val name2 = o2.nameWithoutExtension
                    val a1 =
                        if (name1.contains("-")) name1.substringBeforeLast("-") else name1 + "zzzz"
                    val b1 = if (name1.contains("-")) name1.substringAfterLast("-")
                        .toInt() else Int.MAX_VALUE
                    val a2 =
                        if (name2.contains("-")) name2.substringBeforeLast("-") else name2 + "zzzz"
                    val b2 = if (name2.contains("-")) name2.substringAfterLast("-")
                        .toInt() else Int.MAX_VALUE
                    val compareTo1 = a2.compareTo(a1)
                    if (compareTo1 == 0) {
                        b2.compareTo(b1)
                    } else
                        compareTo1
                } catch (e: Exception) {
                    o1.name.compareTo(o2.name)
                }
            }
        } else
            compareTo
    }
}