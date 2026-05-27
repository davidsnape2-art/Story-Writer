package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Story
import com.example.data.StoryRepository
import com.example.data.CharacterProfile
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.BuildConfig

enum class AiAssistMode {
    CONTINUE, INSPIRE, REWRITE, DESCRIBE, FLESH_OUT
}

sealed interface AiState {
    object Idle : AiState
    object Generating : AiState
    data class Success(val responseText: String, val mode: AiAssistMode) : AiState
    data class Error(val message: String) : AiState
}

class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = StoryRepository(database.storyDao())

    val stories: StateFlow<List<Story>> = repository.allStories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory.asStateFlow()

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

    // Flag for active manual edits in story content during session
    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _editorTitle = MutableStateFlow("")
    val editorTitle: StateFlow<String> = _editorTitle.asStateFlow()

    private var charactersJob: Job? = null
    private val _characters = MutableStateFlow<List<CharacterProfile>>(emptyList())
    val characters: StateFlow<List<CharacterProfile>> = _characters.asStateFlow()

    fun selectStory(story: Story?) {
        _currentStory.value = story
        charactersJob?.cancel()
        if (story != null) {
            _editorContent.value = story.content
            _editorTitle.value = story.title
            charactersJob = viewModelScope.launch {
                repository.getCharactersForStory(story.id).collect {
                    _characters.value = it
                }
            }
        } else {
            _editorContent.value = ""
            _editorTitle.value = ""
            _characters.value = emptyList()
        }
        _aiState.value = AiState.Idle
    }

    fun addCharacter(name: String, age: String, traits: String, backstory: String, physicalDesc: String) {
        val currentId = _currentStory.value?.id ?: return
        viewModelScope.launch {
            val element = CharacterProfile(
                storyId = currentId,
                name = name.ifBlank { "Unknown Character" },
                age = age.ifBlank { "Unknown" },
                traits = traits.ifBlank { "Mysterious" },
                backstory = backstory,
                physicalDesc = physicalDesc
            )
            repository.insertCharacter(element)
        }
    }

    fun updateCharacter(char: CharacterProfile) {
        viewModelScope.launch {
            repository.updateCharacter(char)
        }
    }

    fun deleteCharacter(char: CharacterProfile) {
        viewModelScope.launch {
            repository.deleteCharacter(char)
        }
    }

    fun updatePlotOutline(
        intro: String,
        rising: String,
        climax: String,
        falling: String,
        resolution: String
    ) {
        viewModelScope.launch {
            _currentStory.value?.let { story ->
                val updated = story.copy(
                    plotIntro = intro,
                    plotRisingAction = rising,
                    plotClimax = climax,
                    plotFallingAction = falling,
                    plotResolution = resolution,
                    updatedAt = System.currentTimeMillis()
                )
                _currentStory.value = updated
                repository.update(updated)
            }
        }
    }

    fun updateEditorContent(content: String) {
        _editorContent.value = content
        // Auto-save behavior to keep Room database synced
        viewModelScope.launch {
            _currentStory.value?.let { story ->
                val updated = story.copy(
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
                _currentStory.value = updated
                repository.update(updated)
            }
        }
    }

    fun updateEditorTitle(title: String) {
        _editorTitle.value = title
        viewModelScope.launch {
            _currentStory.value?.let { story ->
                val updated = story.copy(
                    title = title,
                    updatedAt = System.currentTimeMillis()
                )
                _currentStory.value = updated
                repository.update(updated)
            }
        }
    }

    fun createNewStory(title: String, genre: String, description: String, coverSeed: Int) {
        viewModelScope.launch {
            val newStory = Story(
                title = title.ifBlank { "Untitled Story" },
                genre = genre.ifBlank { "General" },
                description = description.ifBlank { "A tale of mystery, magic, and destiny." },
                content = "",
                coverStyleSeed = coverSeed,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val generatedId = repository.insert(newStory)
            val savedStory = repository.getStoryById(generatedId)
            selectStory(savedStory)
        }
    }

    fun deleteStory(story: Story) {
        viewModelScope.launch {
            if (_currentStory.value?.id == story.id) {
                selectStory(null)
            }
            repository.delete(story)
        }
    }

    fun clearAiState() {
        _aiState.value = AiState.Idle
    }

    // Call Gemini API REST
    fun requestAiAssist(mode: AiAssistMode, optionalTone: String = "") {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _aiState.value = AiState.Error("API Key is missing. Please set your GEMINI_API_KEY in the Secrets panel.")
            return
        }

        val storyTitle = _editorTitle.value
        val storyGenre = _currentStory.value?.genre ?: "General"
        val currentText = _editorContent.value

        val plotIntro = _currentStory.value?.plotIntro ?: ""
        val plotRising = _currentStory.value?.plotRisingAction ?: ""
        val plotClimax = _currentStory.value?.plotClimax ?: ""
        val plotFalling = _currentStory.value?.plotFallingAction ?: ""
        val plotResolution = _currentStory.value?.plotResolution ?: ""

        val hasOutline = plotIntro.isNotBlank() || plotRising.isNotBlank() || plotClimax.isNotBlank() || plotFalling.isNotBlank() || plotResolution.isNotBlank()
        val outlineContext = if (hasOutline) {
            """
            Story Outline:
            - Intro/Key Events: $plotIntro
            - Rising Action: $plotRising
            - Climax: $plotClimax
            - Falling Action: $plotFalling
            - Resolution: $plotResolution
            """.trimIndent()
        } else ""

        val chars = _characters.value
        val charactersContext = if (chars.isNotEmpty()) {
            "Active Characters in Story:\n" + chars.joinToString("\n") { c ->
                "- ${c.name} (Age: ${c.age}, Traits: ${c.traits}):\n  Backstory: ${c.backstory}\n  Appearance: ${c.physicalDesc}"
            }
        } else ""

        val fullContext = """
            $charactersContext
            
            $outlineContext
        """.trimIndent()

        _aiState.value = AiState.Generating

        viewModelScope.launch {
            val (systemInstructionText, promptText) = when (mode) {
                AiAssistMode.CONTINUE -> {
                    Pair(
                        "You are an expert novelist and creative writer. Your task is to continue the story seamlessly based on the style, genre, tone, and character voices of the provided text. Write the next natural paragraph or logical development. Do not write any prefacing comments, meta-explanations, warnings, or surrounding content; strictly write ONLY raw fictional content.\n\nUse the following active script details (characters, overall plot structure) for absolute consistency:\n$fullContext",
                        "Title: $storyTitle\nGenre: $storyGenre\nContent:\n$currentText\n\nContinue story:"
                    )
                }
                AiAssistMode.INSPIRE -> {
                    Pair(
                        "You are a creative writing coach. Read the current story and brainstorm exactly 3 exciting, contrasting plot twists, character actions, or mysteries that could happen next. Keep each idea concise, inspiring, and formatted clearly as ideas 1, 2, and 3. Speak in an encouraging, writer-to-writer tone.\n\nRefer to the story's characters & plot plan context:\n$fullContext",
                        "Title: $storyTitle\nGenre: $storyGenre\nContent:\n$currentText\n\nBrainstorm 3 next developments:"
                    )
                }
                AiAssistMode.REWRITE -> {
                    Pair(
                        "You are a literary style editor. Take the given paragraph or text fragment and rewrite it to emphasize a specific tone: $optionalTone. Maintain the plot, characters, and meaning, but heighten the linguistic styling (e.g., suspense, humor, poetry). Output only the revised text, with absolutely no notes or extra text.\n\nRefer to character sheets to maintain consistent voice:\n$fullContext",
                        "Rewrite text with $optionalTone styling:\n$currentText"
                    )
                }
                AiAssistMode.DESCRIBE -> {
                    Pair(
                        "You are a descriptive novelist who excels at sensory overload. Enhance the last action or element of the text by inserting immersive details of sight, sound, raw scent, tactile textures, and ambient heat. Provide only the polished descriptive paragraph, starting directly with the sensory enhancement, without any greeting or confirmation.\n\nKeep physical traits consistent with character profiles:\n$fullContext",
                        "Incorporate sensory description based on the scene so far:\n$currentText"
                    )
                }
                AiAssistMode.FLESH_OUT -> {
                    Pair(
                        "You are an expert descriptive novelist. Take the outline element or plot point and develop it into a rich scene or logical narrative chunk in beautiful prose. Integrate the active characters' personalities and backstory where appropriate and fit the story's genre. Write the story scene directly with no meta-introductions, prefaces, or endings.\n\nRefer to active characters / outline contexts:\n$fullContext",
                        "Flesh out the following outline section into rich prose: [$optionalTone]\n\nStory Title: $storyTitle\nGenre: $storyGenre\nCurrent text context:\n$currentText"
                    )
                }
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = promptText)))
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.8f,
                    maxOutputTokens = 800
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
            )

            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!generatedText.isNullOrBlank()) {
                    _aiState.value = AiState.Success(generatedText.trim(), mode)
                } else {
                    _aiState.value = AiState.Error("Gemini returned empty text response. Check model configuration.")
                }
            } catch (e: Exception) {
                _aiState.value = AiState.Error(e.localizedMessage ?: "Network request failed. Try checking your internet connection.")
            }
        }
    }

    fun appendAiText(text: String) {
        val current = _editorContent.value
        val joined = if (current.isEmpty() || current.endsWith("\n")) {
            current + text
        } else {
            current + "\n\n" + text
        }
        updateEditorContent(joined)
        clearAiState()
    }
}
