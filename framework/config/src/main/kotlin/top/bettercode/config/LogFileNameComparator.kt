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
                    val contains1 = name1.contains("-")
                    val a1 =
                        if (contains1) name1.substringBefore("-") else name1.substringBefore(
                            "."
                        )
                    val contains2 = name2.contains("-")
                    val a2 =
                        if (contains2) name2.substringBefore("-") else name2.substringBefore(
                            "."
                        )
                    val compareTo1 = a1.compareTo(a2)
                    if (compareTo1 == 0) {
                        val compareTo2 = o2.lastModified().compareTo(o1.lastModified())
                        if (compareTo2 == 0) {
                            contains1.compareTo(contains2)
                        } else
                            compareTo2
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