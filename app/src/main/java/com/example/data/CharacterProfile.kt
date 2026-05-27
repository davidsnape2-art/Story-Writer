package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_profiles")
data class CharacterProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: Long,
    val name: String,
    val age: String,
    val traits: String,
    val backstory: String,
    val physicalDesc: String
)
