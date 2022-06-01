package top.bettercode.lang
import java.util.Locale


fun CharSequence.capitalized(): String =
    when {
        isEmpty() -> ""
        else -> get(0).let { initial ->
            when {
                initial.isLowerCase() -> initial.titlecase(Locale.getDefault()) + substring(1)
                else -> toString()
            }
        }
    }

fun CharSequence.decapitalized(): String =
    when {
        isEmpty() -> ""
        else -> get(0).let { initial ->
            when {
                initial.isUpperCase() -> initial.lowercase(Locale.getDefault()) + substring(1)
                else -> toString()
            }
        }
    }
