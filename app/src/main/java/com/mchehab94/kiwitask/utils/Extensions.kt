package com.mchehab94.kiwitask.utils

import java.text.Normalizer
import java.util.*

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 */
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

/**
 * Capitalize one or more words in a String
 */
fun String.capitalizeAll(): String {
    return this.split(" ").joinToString(" ") { it.capitalized() }
}