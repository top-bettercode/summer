package top.bettercode.summer.tools.lang


fun CharSequence.capitalized(): String =
        when {
            isEmpty() -> ""
            else -> get(0).let { initial ->
                when {
                    initial.isLowerCase() -> initial.toUpperCase() + substring(1)
                    else -> toString()
                }
            }
        }

fun CharSequence.decapitalized(): String =
        when {
            isEmpty() -> ""
            else -> get(0).let { initial ->
                when {
                    initial.isUpperCase() -> initial.toLowerCase() + substring(1)
                    else -> toString()
                }
            }
        }
