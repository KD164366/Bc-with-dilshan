package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorialDao {
    @Query("SELECT * FROM tutorials ORDER BY id DESC")
    fun getAllTutorials(): Flow<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE grade = :grade ORDER BY id DESC")
    fun getTutorialsByGrade(grade: String): Flow<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE isDownloaded = 1 ORDER BY id DESC")
    fun getDownloadedTutorials(): Flow<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteTutorials(): Flow<List<Tutorial>>

    @Query("SELECT * FROM tutorials WHERE id = :id")
    suspend fun getTutorialById(id: Int): Tutorial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorial(tutorial: Tutorial): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tutorials: List<Tutorial>)

    @Update
    suspend fun updateTutorial(tutorial: Tutorial)

    @Delete
    suspend fun deleteTutorial(tutorial: Tutorial)

    @Query("SELECT COUNT(*) FROM tutorials")
    suspend fun getTutorialsCount(): Int
}
