package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class MonthlyTokenManager(context: Context) {

    companion object {
        const val TOTAL_MONTHLY_FREE_TOKENS = 8000
        private const val PREFS_NAME = "akash_token_prefs"
        private const val KEY_REMAINING_TOKENS = "remaining_tokens"
        private const val KEY_LAST_RENEW_MONTH = "last_renew_month"
        private const val KEY_LAST_RENEW_YEAR = "last_renew_year"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _remainingTokens = MutableStateFlow(TOTAL_MONTHLY_FREE_TOKENS)
    val remainingTokens: StateFlow<Int> = _remainingTokens.asStateFlow()

    private val _nextRenewalDate = MutableStateFlow("")
    val nextRenewalDate: StateFlow<String> = _nextRenewalDate.asStateFlow()

    init {
        checkAndPerformMonthlyRenewal()
    }

    /**
     * Checks if a new month has started. If so, automatically renews 8000 tokens for free.
     */
    fun checkAndPerformMonthlyRenewal() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed (0 = Jan, 9 = Oct)

        val lastYear = prefs.getInt(KEY_LAST_RENEW_YEAR, -1)
        val lastMonth = prefs.getInt(KEY_LAST_RENEW_MONTH, -1)

        val isNewMonth = lastYear != currentYear || lastMonth != currentMonth

        if (isNewMonth) {
            // Free monthly renewal to 8000 tokens!
            prefs.edit()
                .putInt(KEY_REMAINING_TOKENS, TOTAL_MONTHLY_FREE_TOKENS)
                .putInt(KEY_LAST_RENEW_YEAR, currentYear)
                .putInt(KEY_LAST_RENEW_MONTH, currentMonth)
                .apply()
            _remainingTokens.value = TOTAL_MONTHLY_FREE_TOKENS
        } else {
            val savedTokens = prefs.getInt(KEY_REMAINING_TOKENS, TOTAL_MONTHLY_FREE_TOKENS)
            _remainingTokens.value = savedTokens
        }

        updateNextRenewalDate()
    }

    private fun updateNextRenewalDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1)
        val nextMonth = calendar.get(Calendar.MONTH)
        val nextYear = calendar.get(Calendar.YEAR)

        val monthNameBengali = when (nextMonth) {
            0 -> "জানুয়ারি"
            1 -> "ফেব্রুয়ারি"
            2 -> "মার্চ"
            3 -> "এপ্রিল"
            4 -> "মে"
            5 -> "জুন"
            6 -> "জুলাই"
            7 -> "আগস্ট"
            8 -> "সেপ্টেম্বর"
            9 -> "অক্টোবর"
            10 -> "নভেম্বর"
            11 -> "ডিসেম্বর"
            else -> "পরবর্তী মাস"
        }

        _nextRenewalDate.value = "১লা $monthNameBengali, $nextYear"
    }

    /**
     * Estimates tokens for prompt and AI response, deducting from monthly budget.
     */
    fun recordTokenUsage(promptText: String, responseText: String) {
        val promptTokens = estimateTokens(promptText)
        val responseTokens = estimateTokens(responseText)
        val totalUsed = promptTokens + responseTokens

        val current = _remainingTokens.value
        val updated = (current - totalUsed).coerceAtLeast(0)

        _remainingTokens.value = updated
        prefs.edit().putInt(KEY_REMAINING_TOKENS, updated).apply()
    }

    /**
     * Estimates tokens based on character length and word count in Bengali/English.
     */
    fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0
        // In Bengali script, roughly 1 token ≈ 3 to 4 characters or ~1.3 tokens per word
        val words = text.trim().split(Regex("\\s+")).size
        return (words * 1.3).toInt().coerceAtLeast(1)
    }

    fun hasAvailableTokens(): Boolean {
        checkAndPerformMonthlyRenewal()
        return _remainingTokens.value > 0
    }

    /**
     * Explicitly renews/refreshes the 8,000 monthly free tokens.
     */
    fun renewFreeTokensNow() {
        prefs.edit().putInt(KEY_REMAINING_TOKENS, TOTAL_MONTHLY_FREE_TOKENS).apply()
        _remainingTokens.value = TOTAL_MONTHLY_FREE_TOKENS
    }
}
