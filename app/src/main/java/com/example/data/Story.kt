package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val genre: String,
    val description: String = "",
    val coverStyleSeed: Int = 0, // 0 to 5 for beautiful custom style profiles
    val plotIntro: String = "",
    val plotRisingAction: String = "",
    val plotClimax: String = "",
    val plotFallingAction: String = "",
    val plotResolution: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
