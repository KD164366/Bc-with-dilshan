package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val datePosted: String = "2026-07-30",
    val gradeCategory: String = "All", // "All", "Grade 12", "Grade 13"
    val isImportant: Boolean = true
)
