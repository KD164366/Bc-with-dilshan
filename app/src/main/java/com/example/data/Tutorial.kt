package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutorials")
data class Tutorial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val grade: String, // "Grade 12" or "Grade 13"
    val subject: String, // "Biology", "Chemistry", "Physics", "Combined Maths", "ICT", "General"
    val pdfUrl: String,
    val fileSizeBytes: Long = 2500000L, // ~2.5MB
    val dateAdded: String = "2026-07-30",
    val author: String = "Dilshan Sir",
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val downloadProgress: Float = 0f,
    val isFavorite: Boolean = false,
    val viewsCount: Int = 120,
    val downloadsCount: Int = 85
)
