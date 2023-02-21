package top.bettercode.summer.config

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
                    if (o1.exists() && o2.exists()) {
                        val name1 = o1.nameWithoutExtension.substringBefore("-")
                        val name2 = o2.nameWithoutExtension.substringBefore("-")
                        val compareTo1 = name1.compareTo(name2)
                        return if (compareTo1 != 0) {
                            compareTo1
                        } else {
                            o2.lastModified().compareTo(o1.lastModified())
                        }
                    } else {
                        val name1 = o1.nameWithoutExtension.split('-', ':', '.')
                        val name2 = o2.nameWithoutExtension.split('-', ':', '.')
                        val size1 = name1.size
                        val size2 = name2.size
                        if (size1 > size2)
                            name1.forEachIndexed { i, s1 ->
                                if (i >= size2) return 1
                                val s2 = name2[i]
                                val compareTo1 = compareStr(s1, s2)
                                if (compareTo1 != 0)
                                    return compareTo1
                            }
                        else
                            name2.forEachIndexed { i, s2 ->
                                if (i >= size1) return -1
                                val s1 = name1[i]
                                val compareTo1 = compareStr(s1, s2)
                                if (compareTo1 != 0)
                                    return compareTo1
                            }
                    }
                    0
                } catch (e: Exception) {
                    o1.name.compareTo(o2.name)
                }
            }
        } else
            compareTo
    }

    private fun compareStr(s1: String, s2: String): Int {
        val n1 = s1.toIntOrNull()
        val n2 = s2.toIntOrNull()
        return if (n1 != null && n2 != null) n2.compareTo(n1) else s1.compareTo(s2)
    }
}