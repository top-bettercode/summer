package top.bettercode.logging

import java.io.File

/**
 *
 * @author Peter Wu
 */
class LogFileNameComparator : Comparator<File> {

    override fun compare(o1: File, o2: File): Int {
        val compareTo = o1.isFile.compareTo(o2.isFile)
        return if (compareTo == 0) {
            if (o1.isDirectory || o1.name.contains("+")) {
                o1.name.compareTo(o2.name)
            } else {
                try {
                    val name1 = o1.nameWithoutExtension
                    val name2 = o2.nameWithoutExtension
                    val compareTo1 = name1.contains("-").compareTo(name2.contains("-"))
                    if (compareTo1 == 0) {
                        if (name1.contains("-")) {
                            val compareTo2 = name1.substringBeforeLast("-")
                                .compareTo(name2.substringBeforeLast("-"))
                            if (compareTo2 == 0) {
                                name1.substringAfterLast("-").toInt()
                                    .compareTo(name2.substringAfterLast("-").toInt())
                            } else
                                compareTo2
                        } else {
                            name1.compareTo(name2)
                        }
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