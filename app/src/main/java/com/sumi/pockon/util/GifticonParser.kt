package com.sumi.pockon.util

import com.sumi.pockon.data.model.GifticonInfo

object GifticonParser {

    fun parse(text: String): GifticonInfo {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 1. 제외 키워드
        val keywords = listOf("교환처", "사용처", "상품명", "유효기간", "만료기간", "주문번호")
        var candidates = lines.filterNot { line -> keywords.any { it in line } }

        // 2. 유효기간 찾기
        val (endDate, usedInDate) = extractEndDate(candidates)
        candidates = candidates.filter { it != usedInDate }

        // 3. 금액
        val (cash, usedLine) = extractCash(candidates)
        candidates = candidates.filter { it != usedLine }

        // 4. 브랜드 후보: 텍스트 내 가장 많이 반복된 단어
        val brand = extractBrand(candidates)
        candidates = candidates.filter { it != brand }

        // 5. 상품명 후보: 브랜드 외 + 키워드
        val name = extractProductName(candidates)

        return GifticonInfo(
            brand = brand,
            name = name,
            endDate = endDate,
            cash = cash
        )
    }

    private fun extractEndDate(textLines: List<String>): Pair<String, String> {
        val endDateRegex = Regex("""20\d{2}[년.\-/]\s?\d{1,2}[월.\-/]?\s?\d{1,2}[일]?""")
        val matchedLine = textLines.firstOrNull { endDateRegex.containsMatchIn(it) } ?: return "" to ""
        val rawDate = endDateRegex.find(matchedLine)?.value ?: return "" to ""
        val digits = rawDate.replace(Regex("""[^\d]"""), "") // 숫자만 남김
        return if (digits.length == 8) digits to matchedLine else "" to ""
    }

    private fun extractCash(lines: List<String>): Pair<String, String> {
        val cashRegex = Regex("""\b\d{1,3}(,\d{3})+\b""")
        val matchedLine = lines.firstOrNull { cashRegex.containsMatchIn(it) } ?: return "" to ""
        val rawCash = cashRegex.find(matchedLine)?.value ?: return "" to ""
        val digits = rawCash.replace(",", "")
        return digits to matchedLine
    }

    private fun extractBrand(lines: List<String>): String {
        // 브랜드 후보 찾기 (중복 가장 많은 줄)
        val rawBrand = lines.groupingBy { it }.eachCount()
        return rawBrand.maxByOrNull { it.value }?.key ?: "" // 동일한 최댓값이 여러 개일 때, 가장 먼저 등장한 항목을 반환
    }

    private fun extractProductName(lines: List<String>): String {
        val barcodeRegex = Regex("""^\d{11,14}$""") // 바코드 형태 제외
        val onlyDigitsRegex = Regex("""^\d+$""") // 숫자만 있는 형태 제외
        val containsHangul = Regex("[가-힣]")
        val productIndicators = listOf("(", ")", "+", "세트", "원권")
        val productIndicators2 = listOf("금액권", "교환권", "상품권")
        val volumeRegex = Regex("""\d+\s*(ml|L)""", RegexOption.IGNORE_CASE)

        val candidates = lines
            .filterNot { it.isBlank() }
            .filterNot { line -> onlyDigitsRegex.matches(line) || barcodeRegex.matches(line) }
            .filter { line -> containsHangul.containsMatchIn(line) }  // 한글 포함

        // 1순위: 상품 관련 기호 포함된 줄
        val prioritized = candidates.firstOrNull { line -> volumeRegex.containsMatchIn(line) }
            ?: candidates.firstOrNull { line ->
                productIndicators.any { indicator -> line.contains(indicator) }
            }
            ?: candidates.firstOrNull { line ->
                productIndicators2.any { indicator -> line.contains(indicator) }
            }
            ?: candidates.firstOrNull()


        // 2순위: 나머지 후보 중 첫 번째
        return prioritized ?: candidates.firstOrNull() ?: ""
    }
}