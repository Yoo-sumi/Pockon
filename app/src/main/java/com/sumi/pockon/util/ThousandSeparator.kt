package com.sumi.pockon.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

fun thousandSeparatorTransformation(isSymbol: Boolean): VisualTransformation {
    return VisualTransformation { text ->
        val originalText = text.text
        val formattedText = originalText.toLongOrNull()?.let {
            if (isSymbol) DecimalFormat("₩#,###").format(it)
            else DecimalFormat("#,###").format(it)
        } ?: originalText

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset == 0) 0 else formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return text.length
            }
        }

        TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

fun decimalFormat(number: Int): String {
    return DecimalFormat("#,###").format(number)
}