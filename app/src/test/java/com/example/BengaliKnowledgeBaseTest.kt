package com.example

import com.example.data.repository.BengaliKnowledgeBase
import com.example.data.repository.BengaliQuizData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BengaliKnowledgeBaseTest {

    @Test
    fun testSyllabusItems_coverRequiredBooks() {
        val items = BengaliKnowledgeBase.syllabusItems
        assertTrue("Syllabus should have items", items.isNotEmpty())

        val hasSahityaSanchayan = items.any { it.category == "সাহিত্য সঞ্চয়ন" }
        val hasAamAatirBhepu = items.any { it.category == "আম আঁটির ভেঁপু" }
        val hasGrammar = items.any { it.category == "ব্যাকরণ" }

        assertTrue("Should include সাহিত্য সঞ্চয়ন", hasSahityaSanchayan)
        assertTrue("Should include আম আঁটির ভেঁপু", hasAamAatirBhepu)
        assertTrue("Should include ব্যাকরণ", hasGrammar)
    }

    @Test
    fun testQuizData_hasValidQuestionsAndOptions() {
        val quizSets = BengaliQuizData.allQuizzes
        assertTrue("Should have chapter quiz sets", quizSets.isNotEmpty())

        for (set in quizSets) {
            assertTrue("Chapter ${set.chapterTitle} should have questions", set.questions.isNotEmpty())
            for (q in set.questions) {
                assertEquals("Each question must have 4 options", 4, q.options.size)
                assertTrue("Correct index must be within range 0..3", q.correctIndex in 0..3)
                assertTrue("Explanation should not be blank", q.explanation.isNotBlank())
            }
        }
    }

    @Test
    fun testQuickPrompts_notEmpty() {
        val prompts = BengaliKnowledgeBase.getQuickPrompts()
        assertTrue("Quick prompts should not be empty", prompts.isNotEmpty())
        assertTrue("Should contain Aam Aatir Bhepu prompt", prompts.any { it.contains("আম আঁটির ভেঁপু") })
    }
}
