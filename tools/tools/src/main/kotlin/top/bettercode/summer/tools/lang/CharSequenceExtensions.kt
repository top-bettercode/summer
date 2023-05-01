package top.bettercode.summer.tools.lang

import java.util.*


fun CharSequence.capitalized(): String =
    when {
        isEmpty() -> ""
        else -> get(0).let { initial ->
            when {
                initial.isLowerCase() -> initial.uppercaseChar() + substring(1)
                else -> toString()
            }
        }
    }

fun CharSequence.decapitalized(): String =
    when {
        isEmpty() -> ""
        else -> get(0).let { initial ->
            when {
                initial.isUpperCase() -> initial.lowercaseChar() + substring(1)
                else -> toString()
            }
        }
    }
