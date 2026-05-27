package com.example.data

import kotlinx.coroutines.flow.Flow

class StoryRepository(private val storyDao: StoryDao) {
    val allStories: Flow<List<Story>> = storyDao.getAllStories()

    suspend fun getStoryById(id: Long): Story? {
        return storyDao.getStoryById(id)
    }

    suspend fun insert(story: Story): Long {
        return storyDao.insertStory(story)
    }

    suspend fun update(story: Story) {
        storyDao.updateStory(story)
    }

    suspend fun delete(story: Story) {
        storyDao.deleteStory(story)
    }

    fun getCharactersForStory(storyId: Long): Flow<List<CharacterProfile>> {
        return storyDao.getCharactersForStory(storyId)
    }

    suspend fun insertCharacter(character: CharacterProfile): Long {
        return storyDao.insertCharacter(character)
    }

    suspend fun updateCharacter(character: CharacterProfile) {
        storyDao.updateCharacter(character)
    }

    suspend fun deleteCharacter(character: CharacterProfile) {
        storyDao.deleteCharacter(character)
    }
}
