package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Story
import com.example.data.CharacterProfile
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.*

// Global design palette to match the Frosted Glass HTML template exactly
object FrostedTheme {
    val Background = Color(0xFFF0F4F8) // Light blue-grey background
    val TextPrimary = Color(0xFF1B1B1F) // High-contrast Charcoal
    val TextSecondary = Color(0xFF44474E) // Muted Slate
    val BorderColor = Color(0xFFC4C6D0) // Card outline
    val LineColor = Color(0xFFE1E2E9)   // Separator line
    
    // Aurora Gradient of Scribe AI
    val ScribeGradient = Brush.linearGradient(
        listOf(
            Color(0xFF8E75FF), // Vivid Violet
            Color(0xFF4F91FF), // Deep Sky Blue
            Color(0xFFD195FF)  // Dreamy Orchid Pink
        )
    )

    // Dynamic Button Highlight Theme color
    val ButtonBlueBg = Color(0xFFD1E1FF)
    val ButtonBlueText = Color(0xFF001D49)
}

// Custom book cover color templates
fun getCoverBrush(seed: Int): Brush {
    return when (seed % 6) {
        0 -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))) // Cosmic Slate
        1 -> Brush.linearGradient(listOf(Color(0xFF854D0E), Color(0xFF451A03))) // Autumn Timber
        2 -> Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF022C22))) // Forest Jade
        3 -> Brush.linearGradient(listOf(Color(0xFF6B21A8), Color(0xFF3B0764))) // Regal Velvet
        4 -> Brush.linearGradient(listOf(Color(0xFF9F1239), Color(0xFF4C0519))) // Crimson Rose
        5 -> Brush.linearGradient(listOf(Color(0xFF075985), Color(0xFF082F49))) // Oceanic Deep
        else -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
    }
}

fun getCoverAccent(seed: Int): Color {
    return when (seed % 6) {
        0 -> Color(0xFF38BDF8) // Sky
        1 -> Color(0xFFFDBA74) // Saffron
        2 -> Color(0xFF34D399) // Mint
        3 -> Color(0xFFC084FC) // Lilac
        4 -> Color(0xFFFDA4AF) // Rose
        5 -> Color(0xFF7DD3FC) // Arctic
        else -> Color(0xFF94A3B8)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy · H:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun StoryAppMain(viewModel: StoryViewModel) {
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val currentStory by viewModel.currentStory.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
    val editorTitle by viewModel.editorTitle.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FrostedTheme.Background
    ) {
        if (isExpanded) {
            // Adaptive design: Side-by-side split screen
            Row(modifier = Modifier.fillMaxSize()) {
                // Frosted Glass Shelf Navigation Panel
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .border(
                            width = 1.dp,
                            color = Color(0x1B000000),
                            shape = RoundedCornerShape(0.dp)
                        )
                        .background(FrostedTheme.Background)
                ) {
                    ShelfScreen(
                        stories = stories,
                        selectedStory = currentStory,
                        onSelectStory = { viewModel.selectStory(it) },
                        onInitiateCreate = { showCreateDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Writer Canvas Center stage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(FrostedTheme.Background)
                ) {
                    if (currentStory != null) {
                        EditorScreen(
                            story = currentStory!!,
                            editorTitle = editorTitle,
                            editorContent = editorContent,
                            aiState = aiState,
                            characters = characters,
                            onUpdateTitle = { viewModel.updateEditorTitle(it) },
                            onUpdateContent = { viewModel.updateEditorContent(it) },
                            onRequestAi = { mode, tone -> viewModel.requestAiAssist(mode, tone) },
                            onAppendText = { viewModel.appendAiText(it) },
                            onDismissAiState = { viewModel.clearAiState() },
                            onDelete = { viewModel.deleteStory(it) },
                            onBackToShelf = { viewModel.selectStory(null) },
                            showBackButtonForCompact = false,
                            onAddCharacter = { name, age, traits, backstory, physicalDesc ->
                                viewModel.addCharacter(name, age, traits, backstory, physicalDesc)
                            },
                            onUpdateCharacter = { viewModel.updateCharacter(it) },
                            onDeleteCharacter = { viewModel.deleteCharacter(it) },
                            onUpdatePlotOutline = { intro, rising, climax, falling, resolution ->
                                viewModel.updatePlotOutline(intro, rising, climax, falling, resolution)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Empty / Call to Action welcome state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(36.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(FrostedTheme.ScribeGradient)
                                    .shadow(elevation = 12.dp, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "✦",
                                    color = Color.White,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Welcome to Scribe AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FrostedTheme.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a manuscript from your shelf or initiate a new legend. Scribe AI will guide you and suggest plot points through the power of Gemini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FrostedTheme.TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 380.dp),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile Compact Mode: Screen swapping pattern
            Crossfade(targetState = currentStory != null, label = "ScreenSwapState") { hasActiveStory ->
                if (hasActiveStory) {
                    EditorScreen(
                        story = currentStory!!,
                        editorTitle = editorTitle,
                        editorContent = editorContent,
                        aiState = aiState,
                        characters = characters,
                        onUpdateTitle = { viewModel.updateEditorTitle(it) },
                        onUpdateContent = { viewModel.updateEditorContent(it) },
                        onRequestAi = { mode, tone -> viewModel.requestAiAssist(mode, tone) },
                        onAppendText = { viewModel.appendAiText(it) },
                        onDismissAiState = { viewModel.clearAiState() },
                        onDelete = { viewModel.deleteStory(it) },
                        onBackToShelf = { viewModel.selectStory(null) },
                        showBackButtonForCompact = true,
                        onAddCharacter = { name, age, traits, backstory, physicalDesc ->
                            viewModel.addCharacter(name, age, traits, backstory, physicalDesc)
                        },
                        onUpdateCharacter = { viewModel.updateCharacter(it) },
                        onDeleteCharacter = { viewModel.deleteCharacter(it) },
                        onUpdatePlotOutline = { intro, rising, climax, falling, resolution ->
                            viewModel.updatePlotOutline(intro, rising, climax, falling, resolution)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ShelfScreen(
                        stories = stories,
                        selectedStory = null,
                        onSelectStory = { viewModel.selectStory(it) },
                        onInitiateCreate = { showCreateDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Create manuscript helper Dialog
        if (showCreateDialog) {
            CreateStoryDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, genre, desc, coverSeed ->
                    viewModel.createNewStory(title, genre, desc, coverSeed)
                    showCreateDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(
    stories: List<Story>,
    selectedStory: Story?,
    onSelectStory: (Story) -> Unit,
    onInitiateCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterGenre by remember { mutableStateOf("All") }

    val genres = listOf("All", "Fantasy", "Sci-Fi", "Mystery", "Romance", "Adventure", "General")

    val filteredStories = stories.filter {
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) || 
                            it.description.contains(searchQuery, ignoreCase = true)
        val matchesGenre = selectedFilterGenre == "All" || it.genre.equals(selectedFilterGenre, ignoreCase = true)
        matchesSearch && matchesGenre
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Frosted design branding header block
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FrostedTheme.ScribeGradient)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = "Scribe AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FrostedTheme.TextPrimary
                        )
                        Text(
                            text = "Drafting with Gemini",
                            style = MaterialTheme.typography.bodySmall,
                            color = FrostedTheme.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search glass filter
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search manuscripts...", fontSize = 14.sp, color = FrostedTheme.TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FrostedTheme.TextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xCCFFFFFF),
                        unfocusedContainerColor = Color(0x80FFFFFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .testTag("story_search_bar")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable category shelf filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(genres) { genre ->
                        val isSelected = selectedFilterGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterGenre = genre },
                            label = { Text(genre, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0x4DFFFFFF),
                                labelColor = FrostedTheme.TextSecondary,
                                selectedContainerColor = FrostedTheme.ButtonBlueBg,
                                selectedLabelColor = FrostedTheme.ButtonBlueText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0x1F000000),
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onInitiateCreate,
                containerColor = Color(0xFF8E75FF),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(12.dp, CircleShape)
                    .testTag("floating_add_story_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Manuscript")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (filteredStories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "📘",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matches found" else "Your Shelf is Empty",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = FrostedTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try rephrasing your search or tags." else "Begin a glowing new narrative by pressing the Add button.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = FrostedTheme.TextSecondary,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(filteredStories) { story ->
                    val isSelected = selectedStory?.id == story.id
                    StoryShelfItem(
                        story = story,
                        isSelected = isSelected,
                        onClick = { onSelectStory(story) }
                    )
                }
            }
        }
    }
}

@Composable
fun StoryShelfItem(
    story: Story,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Glass highlight selection frame
    val highlightBorder = if (isSelected) {
        Modifier.border(2.dp, Color(0xFF8E75FF), RoundedCornerShape(20.dp))
    } else {
        Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // Beautiful translucent glass backing
            containerColor = if (isSelected) Color(0xF2FFFFFF) else Color(0x99FFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(highlightBorder)
            .clickable(onClick = onClick)
            .testTag("story_card_${story.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gorgeous layered book spine vector
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getCoverBrush(story.coverStyleSeed))
                    .padding(8.dp)
            ) {
                // Shiny cosmic element representing AI assist seeds
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(getCoverAccent(story.coverStyleSeed).copy(alpha = 0.8f))
                        .align(Alignment.BottomEnd)
                )

                Text(
                    text = story.title.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = FrostedTheme.TextPrimary
                )

                Text(
                    text = story.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = FrostedTheme.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Transparent active tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE1E2E9))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = story.genre,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF44474E)
                        )
                    }

                    // Nice metadata stamp
                    Text(
                        text = formatTimestamp(story.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostedTheme.TextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditorScreen(
    story: Story,
    editorTitle: String,
    editorContent: String,
    aiState: AiState,
    characters: List<CharacterProfile>,
    onUpdateTitle: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onRequestAi: (AiAssistMode, String) -> Unit,
    onAppendText: (String) -> Unit,
    onDismissAiState: () -> Unit,
    onDelete: (Story) -> Unit,
    onBackToShelf: () -> Unit,
    showBackButtonForCompact: Boolean,
    onAddCharacter: (String, String, String, String, String) -> Unit,
    onUpdateCharacter: (CharacterProfile) -> Unit,
    onDeleteCharacter: (CharacterProfile) -> Unit,
    onUpdatePlotOutline: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputTitleUser by remember(story.id) { mutableStateOf(editorTitle) }
    var inputContentUser by remember(story.id) { mutableStateOf(editorContent) }

    var showRewriteSheet by remember { mutableStateOf(false) }
    var tonePromptUser by remember { mutableStateOf("Mysterious") }

    var activeTab by remember(story.id) { mutableStateOf("Write") }

    val genrePrompts = mapOf(
        "Fantasy" to "In a city where magic is fueled by forgotten memories, a young thief discovers a copper key that unlocks someone else's childhood...",
        "Sci-Fi" to "A long-abandoned terraforming satellite suddenly replies to Earth's signals, not with data, but with a piece of classical music...",
        "Mystery" to "An antique clockmaker opens a pocket watch brought in for repair, only to find a microscopic, perfectly preserved crime scene inside...",
        "Romance" to "Two rival cartographers are forced to collaborate on mapping a mysterious, newly appeared island that moves every night...",
        "Adventure" to "A coordinate lead engraved on a grandmother's silver compass points directly to the center of an uncharted, boiling whirlpool...",
        "General" to "A collector of lost mail finally receives a letter addressed to them, postmarked forty years in the future..."
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButtonForCompact) {
                    IconButton(
                        onClick = onBackToShelf,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x1F000000)),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("editor_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list",
                            tint = FrostedTheme.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // High fidelity title input field
                BasicTextField(
                    value = inputTitleUser,
                    onValueChange = {
                        inputTitleUser = it
                        onUpdateTitle(it)
                    },
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = FrostedTheme.TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .testTag("editor_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Delete manuscript button
                IconButton(
                    onClick = {
                        onDelete(story)
                        Toast.makeText(context, "Manuscript deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFEE2E2)),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_story_btn")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete manuscript",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Elegant Paper sheet: White, rounded corners, thin borders as per Frosted theme HTML
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, FrostedTheme.BorderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header sheet metadata values
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEEEAFE),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = story.genre.uppercase(),
                                color = Color(0xFF8E75FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Elegant Navigation tabs inside card
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            listOf("Write", "Plot Outline", "Characters").forEach { tab ->
                                val isSelected = activeTab == tab
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { activeTab = tab }
                                        .testTag("tab_${tab.lowercase().replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = tab,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF8E75FF) else FrostedTheme.TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(2.dp)
                                            .background(if (isSelected) Color(0xFF8E75FF) else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }

                    // Classic clean styling visual divider line layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(FrostedTheme.LineColor)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (activeTab) {
                            "Write" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Genre-specific writing prompt / spark block if content is empty
                                    if (inputContentUser.isBlank()) {
                                        val promptText = genrePrompts[story.genre] ?: genrePrompts["General"]!!
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp)
                                                .testTag("prompt_spark_card"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                                            border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFF8E75FF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${story.genre} Scribe Spark Prompt",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF6B21A8),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = promptText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontFamily = FontFamily.Serif,
                                                    fontStyle = FontStyle.Italic,
                                                    color = Color(0xFF581C87),
                                                    lineHeight = 20.sp
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = {
                                                        inputContentUser = promptText
                                                        onUpdateContent(promptText)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.align(Alignment.End).height(32.dp).testTag("insert_spark_button")
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Begin from Spark", fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        if (inputContentUser.isEmpty()) {
                                            Text(
                                                text = "Once upon a time in a faraway land...",
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 16.sp,
                                                lineHeight = 28.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }

                                        // Ink writing workspace
                                        BasicTextField(
                                            value = inputContentUser,
                                            onValueChange = {
                                                inputContentUser = it
                                                onUpdateContent(it)
                                            },
                                            textStyle = TextStyle(
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 16.sp,
                                                lineHeight = 28.sp,
                                                color = FrostedTheme.TextPrimary
                                            ),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp)
                                                .testTag("story_writer_editor")
                                        )
                                    }
                                }
                            }

                            "Plot Outline" -> {
                                PlotOutlineTab(
                                    story = story,
                                    onUpdatePlotOutline = onUpdatePlotOutline,
                                    onRequestFleshOut = { sectionPrompt ->
                                        onRequestAi(AiAssistMode.FLESH_OUT, sectionPrompt)
                                        activeTab = "Write"
                                    }
                                )
                            }

                            "Characters" -> {
                                CharactersTab(
                                    characters = characters,
                                    onAddCharacter = onAddCharacter,
                                    onUpdateCharacter = onUpdateCharacter,
                                    onDeleteCharacter = onDeleteCharacter
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Suggestion and action controls: styled with translucent Frosted Glass base
            if (activeTab == "Write") {
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(
                        // 60% translucent glass style backing matching HTML suggestion box
                        containerColor = Color(0x99FFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(16.dp)
                    ) {
                        // Aurora glowing trigger head banner
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            // Beautiful glowing micro-node loader dot representation
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(FrostedTheme.ScribeGradient)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Suggestion Engine",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F91FF) // High visibility contrast blue
                            )
                        }

                        // Button controls formatted exactly as modern responsive grid layout
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // ✨ Continue
                            Button(
                                onClick = { onRequestAi(AiAssistMode.CONTINUE, "") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FrostedTheme.ButtonBlueBg,
                                    contentColor = FrostedTheme.ButtonBlueText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("ai_btn_continue")
                            ) {
                                Text("✨ Continue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // 👁️ Sensory
                            Button(
                                onClick = { onRequestAi(AiAssistMode.DESCRIBE, "") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFECEFF1),
                                    contentColor = Color(0xFF37474F)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("ai_btn_describe")
                            ) {
                                Text("👁️ Sensory", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // 💡 Brainstorm
                            Button(
                                onClick = { onRequestAi(AiAssistMode.INSPIRE, "") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3E5F5),
                                    contentColor = Color(0xFF4A148C)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("ai_btn_inspire")
                            ) {
                                Text("💡 Spurt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // ✍️ Rephrase
                            Button(
                                onClick = { showRewriteSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE8EAF6),
                                    contentColor = Color(0xFF1A237E)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(40.dp)
                                    .testTag("ai_btn_rewrite")
                            ) {
                                Text("✍️ Rephrase", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // suggestion content output display
                        AnimatedVisibility(
                            visible = aiState != AiState.Idle,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xE6FFFFFF)) // Crisp white card overlay inside glass panel
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x33000000),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                when (aiState) {
                                    is AiState.Generating -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color(0xFF8E75FF),
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "Consulting the infinite muse...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = FrostedTheme.TextPrimary
                                            )
                                        }
                                    }

                                    is AiState.Success -> {
                                        val clipboard = LocalClipboardManager.current
                                        val suggestion = aiState.responseText

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = when (aiState.mode) {
                                                    AiAssistMode.CONTINUE -> "Manuscript Continuation"
                                                    AiAssistMode.INSPIRE -> "Spark Plot Points"
                                                    AiAssistMode.REWRITE -> "Polished Prose Rewrite"
                                                    AiAssistMode.DESCRIBE -> "Immersive Sensory details"
                                                    AiAssistMode.FLESH_OUT -> "Fleshed Out Prose Scene"
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8E75FF)
                                            )

                                            IconButton(
                                                onClick = onDismissAiState,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Dismiss Suggestion",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Displayed proposal
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Serif,
                                            color = FrostedTheme.TextPrimary,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 140.dp)
                                                .testTag("ai_suggestion_display_text")
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Quick action options
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(suggestion))
                                                    Toast.makeText(context, "Copied proposal text", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Copy Text", fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = { onAppendText(suggestion) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1.3f)
                                                    .testTag("append_ai_suggestion_btn")
                                            ) {
                                                Text("Append Draft", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    is AiState.Error -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = aiState.message,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = onDismissAiState) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lens rewrite select tool
        if (showRewriteSheet) {
            TonePickerDialog(
                currentTone = tonePromptUser,
                onSelectTone = { tonePromptUser = it },
                onConfirm = {
                    showRewriteSheet = false
                    onRequestAi(AiAssistMode.REWRITE, tonePromptUser)
                },
                onDismiss = { showRewriteSheet = false }
            )
        }
    }
}

@Composable
fun PlotOutlineTab(
    story: Story,
    onUpdatePlotOutline: (String, String, String, String, String) -> Unit,
    onRequestFleshOut: (String) -> Unit
) {
    var intro by remember(story.id) { mutableStateOf(story.plotIntro) }
    var rising by remember(story.id) { mutableStateOf(story.plotRisingAction) }
    var climax by remember(story.id) { mutableStateOf(story.plotClimax) }
    var falling by remember(story.id) { mutableStateOf(story.plotFallingAction) }
    var resolution by remember(story.id) { mutableStateOf(story.plotResolution) }

    fun save() {
        onUpdatePlotOutline(intro, rising, climax, falling, resolution)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Structure Your Narrative Arc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FrostedTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Draft your plot nodes to establish structural guidance. Gemini references this map during continue requests.",
                style = MaterialTheme.typography.bodySmall,
                color = FrostedTheme.TextSecondary
            )
        }

        val items = listOf(
            Triple("1. Exposition / Intro", intro, { s: String -> intro = s; save() }),
            Triple("2. Rising Action", rising, { s: String -> rising = s; save() }),
            Triple("3. Climax", climax, { s: String -> climax = s; save() }),
            Triple("4. Falling Action", falling, { s: String -> falling = s; save() }),
            Triple("5. Resolution", resolution, { s: String -> resolution = s; save() })
        )

        items(items) { (label, value, onValueChange) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFD)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1)
                        )

                        if (value.isNotBlank()) {
                            Button(
                                onClick = { onRequestFleshOut("$label: $value") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("flesh_out_${label.lowercase().replace(" ", "_").substringBefore(".")}")
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Flesh out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = { Text("What happens in this milestone?", fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 14.sp, color = FrostedTheme.TextPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_${label.lowercase().replace(" ", "_").substringBefore(".")}")
                    )
                }
            }
        }
    }
}

@Composable
fun CharactersTab(
    characters: List<CharacterProfile>,
    onAddCharacter: (String, String, String, String, String) -> Unit,
    onUpdateCharacter: (CharacterProfile) -> Unit,
    onDeleteCharacter: (CharacterProfile) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editChar by remember { mutableStateOf<CharacterProfile?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (characters.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cast Your Characters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No characters added yet. Create physical profiles, backstories, and traits for Gemini to reference during story generations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FrostedTheme.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_character_empty_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add First Character", fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Dramatis Personae",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FrostedTheme.TextPrimary
                            )
                            Text(
                                text = "${characters.size} Character profile sheets registered",
                                style = MaterialTheme.typography.bodySmall,
                                color = FrostedTheme.TextSecondary
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp).testTag("add_character_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(characters) { char ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFD)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth().testTag("character_card_${char.name.lowercase().replace(" ", "_")}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Character Avatar badge (using initials)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFEEFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.name.filter { it.isUpperCase() }.take(2).ifEmpty { char.name.take(2) }.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD946EF),
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = char.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FrostedTheme.TextPrimary
                                    )
                                    Text(
                                        text = "Age: ${char.age} · Traits: ${char.traits}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FrostedTheme.TextSecondary
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editChar = char },
                                        modifier = Modifier.size(36.dp).testTag("edit_char_${char.name.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Character",
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCharacter(char) },
                                        modifier = Modifier.size(36.dp).testTag("delete_char_${char.name.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Character",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (char.physicalDesc.isNotBlank() || char.backstory.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFE2E8F0))
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (char.physicalDesc.isNotBlank()) {
                                    Text(
                                        text = "Appearance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = char.physicalDesc,
                                        fontSize = 12.sp,
                                        color = FrostedTheme.TextSecondary,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                if (char.backstory.isNotBlank()) {
                                    Text(
                                        text = "Backstory",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = char.backstory,
                                        fontSize = 12.sp,
                                        color = FrostedTheme.TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            CharacterFormDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, age, traits, backstory, physicalDesc ->
                    onAddCharacter(name, age, traits, backstory, physicalDesc)
                    showAddDialog = false
                }
            )
        }

        if (editChar != null) {
            CharacterFormDialog(
                char = editChar,
                onDismiss = { editChar = null },
                onSave = { name, age, traits, backstory, physicalDesc ->
                    onUpdateCharacter(editChar!!.copy(
                        name = name,
                        age = age,
                        traits = traits,
                        backstory = backstory,
                        physicalDesc = physicalDesc
                    ))
                    editChar = null
                }
            )
        }
    }
}

@Composable
fun CharacterFormDialog(
    char: CharacterProfile? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(char?.name ?: "") }
    var age by remember { mutableStateOf(char?.age ?: "") }
    var traits by remember { mutableStateOf(char?.traits ?: "") }
    var physicalDesc by remember { mutableStateOf(char?.physicalDesc ?: "") }
    var backstory by remember { mutableStateOf(char?.backstory ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (char == null) "Create Character Sheet" else "Edit Character Sheet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FrostedTheme.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    Text("Character Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FrostedTheme.TextPrimary)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("E.g., Lyra Cole", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("form_char_name")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Age", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FrostedTheme.TextPrimary)
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                placeholder = { Text("E.g., 28", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("form_char_age")
                            )
                        }
                        Column(modifier = Modifier.weight(2f)) {
                            Text("Personality Traits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FrostedTheme.TextPrimary)
                            OutlinedTextField(
                                value = traits,
                                onValueChange = { traits = it },
                                placeholder = { Text("E.g., Sarcastic, intensely loyal", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("form_char_traits")
                            )
                        }
                    }
                }

                item {
                    Text("Physical Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FrostedTheme.TextPrimary)
                    OutlinedTextField(
                        value = physicalDesc,
                        onValueChange = { physicalDesc = it },
                        placeholder = { Text("E.g., Silver-streaked hazel hair, mechanical brace", fontSize = 13.sp) },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("form_char_phys")
                    )
                }

                item {
                    Text("Backstory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FrostedTheme.TextPrimary)
                    OutlinedTextField(
                        value = backstory,
                        onValueChange = { backstory = it },
                        placeholder = { Text("E.g., Apprentice who unlocked the celestial library's gate...", fontSize = 13.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("form_char_backstory")
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = FrostedTheme.TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(name, age, traits, backstory, physicalDesc) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF)),
                            modifier = Modifier.testTag("form_char_save_btn")
                        ) {
                            Text("Save Sheet")
                        }
                    }
                }
            }
        }
    }
}

// Dialog for switching Rephrasing Lenses
@Composable
fun TonePickerDialog(
    currentTone: String,
    onSelectTone: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val tonesList = listOf(
        Pair("Mysterious 🌌", "Add dark fantasy tension, shadowy ambiance, and ominous hooks."),
        Pair("Poetic & Vivid 🌸", "Add dense sensory metaphors, beautiful rhymes, and lyrical flows."),
        Pair("Suspenseful ⏱️", "Heighten the pacing with short, breathy beats and anticipation."),
        Pair("Humorous / Satiric 🎭", "Sprinkle light humor, dry wit, or witty societal feedback."),
        Pair("Crisp & Concise 📄", "Prune redundant phrases for ultra-polished impact writing.")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Refining Tone Preset",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                Text(
                    text = "Select a stylistic lens to re-envision your drafted paragraph draft.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FrostedTheme.TextSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(tonesList) { item ->
                        val isSelected = currentTone.startsWith(item.first.split(" ").first())
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFFF3E5F5)
                                    else Color.Transparent
                                )
                                .clickable { onSelectTone(item.first) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = item.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(0xFF4A148C) else FrostedTheme.TextPrimary
                                )
                                Text(
                                    text = item.second,
                                    fontSize = 11.sp,
                                    color = FrostedTheme.TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = FrostedTheme.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF))
                    ) {
                        Text("Rephrase")
                    }
                }
            }
        }
    }
}

// Dialog for starting a new masterpiece
@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, genre: String, description: String, coverSeed: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Fantasy") }
    var description by remember { mutableStateOf("") }
    var coverSeed by remember { mutableStateOf(0) }

    val genresList = listOf("Fantasy", "Sci-Fi", "Mystery", "Romance", "Adventure", "General")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Begin a New Legend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                Text(
                    text = "A blank page is a portal to endless worlds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FrostedTheme.TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title Input
                Text(
                    text = "Story Title",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("E.g., Sky of Echoes", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("dialog_story_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Genre picker scroll
                Text(
                    text = "Genre",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    items(genresList) { genreOption ->
                        val isSelected = genre == genreOption
                        InputChip(
                            selected = isSelected,
                            onClick = { genre = genreOption },
                            label = { Text(genreOption, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom cover pickers representation
                Text(
                    text = "Manuscript Cover Style",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0..5) {
                        val isSelected = coverSeed == i
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(getCoverBrush(i))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF8E75FF) else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { coverSeed = i }
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Short Pitch / Description
                Text(
                    text = "Synopsis / Pitch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FrostedTheme.TextPrimary
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("E.g., A sky-sailor discovers a rhythmic ancient distress pulse call in the nebula.", fontSize = 13.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("dialog_story_synopsis_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = FrostedTheme.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onCreate(title, genre, description, coverSeed) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E75FF)),
                        modifier = Modifier.testTag("dialog_confirm_create_btn")
                    ) {
                        Text("Open Quill")
                    }
                }
            }
        }
    }
}
