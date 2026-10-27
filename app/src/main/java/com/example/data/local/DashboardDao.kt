package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM chapter_progress")
    fun getAllChapterProgress(): Flow<List<ChapterProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChapter(chapter: ChapterProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChapters(chapters: List<ChapterProgressEntity>)

    @Query("UPDATE chapter_progress SET isCompleted = :isCompleted, completedAt = :completedAt WHERE chapterId = :chapterId")
    suspend fun setChapterCompletion(chapterId: String, isCompleted: Boolean, completedAt: Long)

    @Query("SELECT * FROM quiz_results ORDER BY timestamp ASC")
    fun getAllQuizResultsAsc(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResultsDesc(): Flow<List<QuizResultEntity>>

    @Insert
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Query("SELECT COUNT(*) FROM chapter_progress WHERE isCompleted = 1")
    suspend fun getCompletedChapterCount(): Int

    @Query("SELECT COUNT(*) FROM chapter_progress")
    suspend fun getTotalChapterCount(): Int

    @Query("SELECT AVG(percentage) FROM quiz_results")
    suspend fun getAverageQuizScore(): Float?
}
