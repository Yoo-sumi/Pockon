package com.example.giftbox.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return dateFilter(text)
    }
}

private fun dateFilter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
    var out = ""
    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i == 3  || i == 5) out += "."
    }

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 5) return offset + 1
            if (offset <= 8) return offset + 2
            return 8
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 6) return offset - 1
            if (offset <= 10) return offset - 2
            return 10
        }
    }

    return TransformedText(AnnotatedString(out), numberOffsetTranslator)
}

fun getDday(endDate: String): Pair<String, Boolean> {
    val formatter = DateTimeFormatter.BASIC_ISO_DATE
    val current = LocalDateTime.now().format(formatter)

    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    val startDate = dateFormat.parse(current)?.time
    val parseEndDate = dateFormat.parse(endDate)?.time
    if (parseEndDate != null && startDate != null) {
        val diff = (parseEndDate - startDate) / (24 * 60 * 60 * 1000)
        return if (diff.toInt() > 0) {
            Pair("D-$diff", false)
        } else if (diff.toInt() == 0) {
            Pair("D-Day", false)
        } else {
            Pair("기한만료", true)
        }
    }
    return Pair("", false)
}

fun formatString(endDate: String): String {
    return endDate.mapIndexed { index, c ->
        if (index == 3 || index == 5) "${c}." else c
    }.joinToString("")
}