package top.bettercode.summer.tools.lang


fun CharSequence.capitalized(): String =
        when {
            isEmpty() -> ""
            else -> get(0).let { initial ->
                when {
                    initial.isLowerCase() -> initial.uppercase() + substring(1)
                    else -> toString()
                }
            }
        }

fun CharSequence.decapitalized(): String =
        when {
            isEmpty() -> ""
            else -> get(0).let { initial ->
                when {
                    initial.isUpperCase() -> initial.lowercase() + substring(1)
                    else -> toString()
                }
            }
        }
