package org.fossify.messages.helpers

private const val MIN_CODE_LENGTH = 4
private const val MAX_CODE_LENGTH = 8
private const val YEAR_LENGTH = 4
private const val MIN_YEAR = 1900
private const val MAX_YEAR = 2100
private const val NUMBER_SEPARATORS = " -./,:+()"

private const val SHAPE_NONE = 0
private const val SHAPE_ANSWER = 1
private const val SHAPE_ASSIGNMENT = 2
private const val SHAPE_TIGHT_ASSIGNMENT = 3

private val otpKeywordRegex = Regex(
    "(?<![A-Za-z0-9])(otps?|codes?|c[oóOÓ]digos?|codice|kod[a-z]*|pins?|passcodes?|passwords?" +
        "|verif[a-z]*|one[ -]?time|2fa|two[ -]factor|tokens?|auth(?:entication|enticate)?)" +
        "(?![A-Za-z0-9])|[кК][оО][дД]|验证码|驗證碼",
    RegexOption.IGNORE_CASE
)

private val otpCandidateRegex = Regex(
    "(?<![A-Za-z0-9])(?:[A-Za-z]-)?([A-Za-z0-9]{$MIN_CODE_LENGTH,$MAX_CODE_LENGTH})(?![A-Za-z0-9])"
)

private val nonOtpContextRegex = Regex(
    "(?<![\\p{L}0-9])(tracking|track|discount|promo|promotional|coupon|voucher|offer|referral" +
        "|area|postal|postcode|zip|bar|error|country|dialling|dialing)[^\\p{L}0-9]+$",
    RegexOption.IGNORE_CASE
)

private val tightGapRegex = Regex("^[^\\p{L}0-9]*(\\p{L}{1,3}[^\\p{L}0-9]+)?$")
private val assignmentGapRegex = Regex("^[^\\p{L}0-9]*(\\p{L}{1,12}[^\\p{L}0-9]+){0,3}$")
private val answerGapRegex = Regex("^[^\\p{L}0-9]*(\\p{L}{1,12}[^\\p{L}0-9]+){0,4}$")

fun extractOtp(body: String): String? {
    val keywords = otpKeywordRegex.findAll(body)
        .map { it.range }
        .filterNot { nonOtpContextRegex.containsMatchIn(body.substring(0, it.first)) }
        .toList()
    if (keywords.isEmpty()) {
        return null
    }

    val scored = otpCandidateRegex.findAll(body)
        .filter { it.groupValues[1].any(Char::isDigit) && !isPartOfNumber(body, it.range) }
        .map { match -> match.groupValues[1] to keywords.maxOf { shapeOf(body, it, match.range) } }
        .filter { it.second != SHAPE_NONE }
        .toList()

    val code = scored.filterNot { isYear(it.first) }.maxByOrNull { it.second }
    if (code != null) {
        return code.first
    }

    val year = scored.singleOrNull()
    return if (year?.second == SHAPE_TIGHT_ASSIGNMENT) year.first else null
}

private fun shapeOf(body: String, keyword: IntRange, candidate: IntRange) = when {
    keyword.last < candidate.first -> {
        val gap = body.substring(keyword.last + 1, candidate.first)
        when {
            tightGapRegex.matches(gap) -> SHAPE_TIGHT_ASSIGNMENT
            assignmentGapRegex.matches(gap) -> SHAPE_ASSIGNMENT
            else -> SHAPE_NONE
        }
    }

    candidate.last < keyword.first -> {
        val gap = body.substring(candidate.last + 1, keyword.first)
        if (answerGapRegex.matches(gap)) SHAPE_ANSWER else SHAPE_NONE
    }

    else -> SHAPE_NONE
}

private fun isYear(code: String) = code.length == YEAR_LENGTH &&
    code.all(Char::isDigit) && code.toInt() in MIN_YEAR..MAX_YEAR

private fun isPartOfNumber(body: String, range: IntRange): Boolean {
    val before = body.getOrNull(range.first - 1)
    val after = body.getOrNull(range.last + 1)
    if (after == '%' || before?.category == CharCategory.CURRENCY_SYMBOL) {
        return true
    }

    val groupedLeft = before != null && before in NUMBER_SEPARATORS &&
        body.getOrNull(range.first - 2)?.isDigit() == true
    val groupedRight = after != null && after in NUMBER_SEPARATORS &&
        body.getOrNull(range.last + 2)?.isDigit() == true
    return groupedLeft || groupedRight
}
