package com.gideongeng.music.desktop

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gideongeng.music.innertube.models.*
import com.gideongeng.music.innertube.pages.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// HOME SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homePage: HomePage?,
    isLoading: Boolean,
    selectedChipEndpoint: BrowseEndpoint?,
    onChipClick: (HomePage.Chip?) -> Unit,
    onSongClick: (SongItem) -> Unit,
    onItemClick: (YTItem) -> Unit,
) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"; in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"; else -> "Good Night"
    }

    Box(Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        // Keyboard arrow key scrolling
        LazyColumn(
            Modifier.fillMaxSize().onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionDown -> { /* handled by focus */ false }
                        Key.DirectionUp -> { false }
                        else -> false
                    }
                } else false
            },
            state = listState
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color(0xFF1A1040), Color(0xFF121212))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text("YungMusic", style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(greeting, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Chips row with "All" at the start
            val chips = homePage?.chips
            item {
                        val rowState = rememberLazyListState()
                Box(Modifier.fillMaxWidth()) {
                    LazyRow(
                        state = rowState,
                        modifier = Modifier.sidewaysScroll(rowState),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" chip resets to default home
                        item {
                            FilterChip(
                                selected = selectedChipEndpoint == null,
                                onClick = { onChipClick(null) },
                                label = { Text("All", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF2A2A2A),
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = selectedChipEndpoint == null,
                                    borderColor = Color(0xFF3A3A3A)
                                )
                            )
                        }
                        if (!chips.isNullOrEmpty()) {
                            items(chips) { chip ->
                                FilterChip(
                                    selected = chip.endpoint == selectedChipEndpoint,
                                    onClick = { onChipClick(chip) },
                                    label = { Text(chip.title, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(0xFF2A2A2A),
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.Black
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true, selected = chip.endpoint == selectedChipEndpoint,
                                        borderColor = Color(0xFF3A3A3A)
                                    )
                                )
                            }
                        }
                    }
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(rowState),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 24.dp)
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading your music...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (homePage != null) {
                items(homePage.sections) { section ->
                    HomeSectionRow(section = section) { item ->
                        if (item is SongItem) {
                            val songs = section.items.filterIsInstance<SongItem>().ifEmpty { listOf(item) }
                            AudioPlayer.playQueue(
                                songs,
                                songs.indexOfFirst { it.id == item.id }.coerceAtLeast(0),
                                section.title
                            )
                        } else onItemClick(item)
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.WifiOff, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Could not load home. Check your connection.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}

@Composable
fun HomeSectionRow(section: HomePage.Section, onItemClick: (YTItem) -> Unit) {
    Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                section.label?.let {
                    Text(it.uppercase(), fontSize = 10.sp, color = Color(0xFF888888),
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
                Text(section.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        val rowState = rememberLazyListState()
        LazyRow(
            state = rowState,
            modifier = Modifier.fillMaxWidth().sidewaysScroll(rowState),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(section.items) { item -> HomeItemCard(item = item) { onItemClick(item) } }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(rowState),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp).height(8.dp),
            style = ScrollbarStyle(
                minimalHeight = 8.dp,
                thickness = 6.dp,
                shape = RoundedCornerShape(4.dp),
                hoverDurationMillis = 300,
                unhoverColor = Color(0xFF3A3A3A),
                hoverColor = Color(0xFF6A6A6A)
            )
        )
    }
}

@Composable
fun HomeItemCard(item: YTItem, onClick: () -> Unit) {
    val thumbnail = item.thumbnail
    val title = item.title
    val subtitle = when (item) {
        is SongItem -> item.artists.joinToString(", ") { it.name }
        is AlbumItem -> item.artists?.joinToString(", ") { it.name } ?: ""
        is PlaylistItem -> item.author?.name ?: ""
        is ArtistItem -> "Artist"
        else -> ""
    }
    val isCircle = item is ArtistItem

    Card(
        onClick = onClick, modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            ThumbnailImage(
                url = thumbnail,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    .clip(if (isCircle) CircleShape else RoundedCornerShape(6.dp)),
                iconSize = 32.dp
            )
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank())
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SEARCH SCREEN — shows songs, artists, albums
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(
    query: String, onQueryChange: (String) -> Unit,
    isSearching: Boolean, results: List<YTItem>,
    onSearch: (String) -> Unit,
    onSongClick: (SongItem) -> Unit,
    onItemClick: (YTItem) -> Unit,
) {
    var suggestions by remember { mutableStateOf<com.gideongeng.music.innertube.models.SearchSuggestions?>(null) }
    var committedQuery by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        if (query.length < 2) {
            suggestions = null
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(280)
        suggestions = fetchSearchSuggestions(query)
        if (committedQuery != query) {
            onSearch(query)
        }
    }

    val showSuggestions = query.length >= 2 && committedQuery != query &&
        (suggestions?.queries?.isNotEmpty() == true || suggestions?.recommendedItems?.isNotEmpty() == true)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query, onValueChange = {
                    onQueryChange(it)
                    if (it != committedQuery) committedQuery = ""
                },
                placeholder = { Text("Search songs, artists, albums...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true, modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange(""); committedQuery = ""; suggestions = null }) {
                            Icon(Icons.Filled.Close, "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { committedQuery = query; onSearch(query) })
            )
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                showSuggestions -> {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(vertical = 8.dp)) {
                        val recs = suggestions?.recommendedItems.orEmpty()
                        items(recs.size) { i ->
                            when (val item = recs[i]) {
                                is SongItem -> SongListItem(song = item,
                                    isPlaying = AudioPlayer.currentlyPlaying.value?.id == item.id,
                                    onClick = { onSongClick(item) })
                                is ArtistItem -> ArtistListItem(item = item, onClick = { onItemClick(item) })
                                is AlbumItem -> AlbumListItem(item = item, onClick = { onItemClick(item) })
                                is PlaylistItem -> PlaylistListItem(item = item, onClick = { onItemClick(item) })
                                else -> {}
                            }
                        }
                        items(suggestions?.queries.orEmpty()) { q ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    onQueryChange(q)
                                    committedQuery = q
                                    onSearch(q)
                                }.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Search, null, tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(q, color = Color.White, modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.NorthEast, null, tint = Color(0xFF666666), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    VerticalScrollbar(adapter = rememberScrollbarAdapter(listState),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
                }
                isSearching -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                results.isEmpty() && query.isBlank() -> EmptyState(
                    icon = Icons.Filled.Search, message = "Search for a song, artist, or album")
                results.isEmpty() -> EmptyState(
                    icon = Icons.Filled.SearchOff, message = "No results for \"$query\"")
                else -> {
                    Box(Modifier.fillMaxSize()) {
                        val listState = rememberLazyListState()
                        LazyColumn(Modifier.fillMaxSize(), state = listState,
                            contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(results) { item ->
                                when (item) {
                                    is SongItem -> SongListItem(
                                        song = item,
                                        isPlaying = AudioPlayer.currentlyPlaying.value?.id == item.id,
                                        onClick = { onSongClick(item) }
                                    )
                                    is ArtistItem -> ArtistListItem(item = item, onClick = { onItemClick(item) })
                                    is AlbumItem -> AlbumListItem(item = item, onClick = { onItemClick(item) })
                                    is PlaylistItem -> PlaylistListItem(item = item, onClick = { onItemClick(item) })
                                    else -> {}
                                }
                            }
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(listState),
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ARTIST DETAIL SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArtistDetailScreen(
    page: ArtistPage?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSongClick: (SongItem) -> Unit,
    onItemClick: (YTItem) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var extraItems by remember { mutableStateOf<List<YTItem>>(emptyList()) }
    var loadingMore by remember { mutableStateOf(false) }
    var followed by remember(page?.artist?.id) { mutableStateOf(false) }

    LaunchedEffect(page?.artist?.id, page?.sections) {
        val more = page?.sections?.firstOrNull { it.items.any { item -> item is SongItem } }?.moreEndpoint
            ?: return@LaunchedEffect
        loadingMore = true
        extraItems = fetchArtistItems(more)
        loadingMore = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    if (page == null) {
        Column {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White) }
            EmptyState(Icons.Filled.Person, "Could not load artist")
        }
        return
    }

    val popular = page.sections.firstOrNull { it.items.any { item -> item is SongItem } }
    val albumSections = page.sections.filter { it !== popular }

    Box(Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        LazyColumn(Modifier.fillMaxSize(), state = listState) {
            item {
                Box(Modifier.fillMaxWidth().height(280.dp)) {
                    ThumbnailImage(
                        url = page.artist.thumbnail,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 80.dp
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC121212))
                            )
                        )
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                            .background(Color.Black.copy(0.35f), CircleShape)
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                        Text(page.artist.title, style = MaterialTheme.typography.displaySmall,
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            page.subscriberCountText?.let {
                                Text(it, color = Color(0xFFCCCCCC), style = MaterialTheme.typography.bodyMedium)
                            }
                            page.monthlyListenerCount?.let {
                                Text(it, color = Color(0xFFCCCCCC), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { followed = !followed },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (followed) Color(0xFF2A2A2A) else MaterialTheme.colorScheme.secondary
                        )
                    ) { Text(if (followed) "Following" else "Follow", color = if (followed) Color.White else Color.Black) }
                    OutlinedButton(onClick = {
                        scope.launch {
                            val ep = page.artist.radioEndpoint ?: page.artist.shuffleEndpoint
                            val songs = if (ep != null) fetchRadioSongs(ep)
                            else page.sections.flatMap { it.items.filterIsInstance<SongItem>() }
                            if (songs.isNotEmpty()) AudioPlayer.playQueue(songs, 0, "${page.artist.title} radio")
                        }
                    }) {
                        Icon(Icons.Filled.Podcasts, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start radio")
                    }
                }
            }
            if (popular != null) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(popular.title.ifBlank { "Popular" }, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium, color = Color.White)
                        if (popular.moreEndpoint != null) {
                            TextButton(onClick = {
                                scope.launch {
                                    loadingMore = true
                                    extraItems = fetchArtistItems(popular.moreEndpoint!!)
                                    loadingMore = false
                                }
                            }) { Text("More", color = MaterialTheme.colorScheme.secondary) }
                        }
                    }
                }
                val songs = (if (extraItems.isNotEmpty()) extraItems.filterIsInstance<SongItem>()
                    else popular.items.filterIsInstance<SongItem>())
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = AudioPlayer.currentlyPlaying.value?.id == song.id,
                        onClick = {
                            AudioPlayer.playQueue(songs, songs.indexOf(song).coerceAtLeast(0), page.artist.title)
                        }
                    )
                }
                if (loadingMore) {
                    item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } }
                }
            }
            items(albumSections) { section ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                    val songItems = section.items.filterIsInstance<SongItem>()
                    val otherItems = section.items.filter { it !is SongItem }
                    songItems.forEach { song ->
                        SongListItem(
                            song = song,
                            isPlaying = AudioPlayer.currentlyPlaying.value?.id == song.id,
                            onClick = {
                                AudioPlayer.playQueue(songItems, songItems.indexOf(song), section.title)
                            }
                        )
                    }
                    if (otherItems.isNotEmpty()) {
                        val rowState = rememberLazyListState()
                        LazyRow(
                            state = rowState,
                            modifier = Modifier.sidewaysScroll(rowState),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(otherItems) { item ->
                                HomeItemCard(item = item) { onItemClick(item) }
                            }
                        }
                        HorizontalScrollbar(
                            adapter = rememberScrollbarAdapter(rowState),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp).height(8.dp),
                            style = ScrollbarStyle(
                                minimalHeight = 8.dp, thickness = 6.dp, shape = RoundedCornerShape(4.dp),
                                hoverDurationMillis = 300,
                                unhoverColor = Color(0xFF3A3A3A), hoverColor = Color(0xFF6A6A6A)
                            )
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALBUM DETAIL SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AlbumDetailScreen(
    page: AlbumPage?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSongClick: (SongItem) -> Unit,
    onPlayAll: (List<SongItem>) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                page?.album?.title ?: "Album",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }
        if (page == null) {
            EmptyState(Icons.Filled.Album, "Could not load album")
            return@Column
        }

        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                // Album header
                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF1A2040), Color(0xFF121212))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.Top) {
                            ThumbnailImage(
                                url = page.album.thumbnail,
                                modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)),
                                iconSize = 56.dp
                            )
                            Column(Modifier.weight(1f)) {
                                Text("ALBUM", fontSize = 10.sp, color = Color(0xFF888888),
                                    letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                                Text(page.album.title, style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White, fontWeight = FontWeight.Bold)
                                val artist = page.album.artists?.joinToString(", ") { it.name } ?: ""
                                if (artist.isNotBlank())
                                    Text(artist, color = Color(0xFFAAAAAA),
                                        style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { onPlayAll(page.songs) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, null,
                                        tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Song list
                items(page.songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = AudioPlayer.currentlyPlaying.value?.id == song.id,
                        onClick = {
                            AudioPlayer.playQueue(page.songs, page.songs.indexOf(song), page.album.title)
                        }
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PLAYLIST DETAIL SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlaylistDetailScreen(
    page: PlaylistPage?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSongClick: (SongItem) -> Unit,
    onPlayAll: (List<SongItem>) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                page?.playlist?.title ?: "Playlist",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Column
        }
        if (page == null) {
            EmptyState(Icons.AutoMirrored.Filled.QueueMusic, "Could not load playlist")
            return@Column
        }

        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                // Playlist header
                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF1A3020), Color(0xFF121212))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.Top) {
                            ThumbnailImage(
                                url = page.playlist.thumbnail,
                                modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)),
                                iconSize = 56.dp
                            )
                            Column(Modifier.weight(1f)) {
                                Text("PLAYLIST", fontSize = 10.sp, color = Color(0xFF888888),
                                    letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                                Text(page.playlist.title, style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White, fontWeight = FontWeight.Bold)
                                val author = page.playlist.author?.name ?: ""
                                if (author.isNotBlank())
                                    Text(author, color = Color(0xFFAAAAAA),
                                        style = MaterialTheme.typography.bodyMedium)
                                Text("${page.songs.size} songs", color = Color(0xFF888888),
                                    style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { onPlayAll(page.songs) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, null,
                                        tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Song list
                items(page.songs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = AudioPlayer.currentlyPlaying.value?.id == song.id,
                        onClick = {
                            AudioPlayer.playQueue(page.songs, page.songs.indexOf(song), page.playlist.title)
                        }
                    )
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIBRARY SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LibraryScreen(onSongClick: (SongItem) -> Unit) {
    var history by remember { mutableStateOf(emptyList<SongItem>()) }
    var favorites by remember { mutableStateOf(emptyList<SongItem>()) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            history = LibraryManager.getHistory()
        } else {
            favorites = LibraryManager.getFavorites()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Recently Played", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Favorites ♥", modifier = Modifier.padding(16.dp))
            }
        }
        
        Spacer(Modifier.height(16.dp))
        val currentList = if (selectedTab == 0) history else favorites

        if (currentList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selectedTab == 0) Icons.Filled.History else Icons.Filled.FavoriteBorder,
                        null, tint = Color(0xFF555555), modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (selectedTab == 0) "Your history is empty.\nPlay some songs to see them here."
                        else "No favorites yet.\nHeart a song to save it here.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize(), state = listState) {
                    items(currentList) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = AudioPlayer.currentlyPlaying.value?.id == song.id,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIST ITEMS for non-song types
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArtistListItem(item: ArtistItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThumbnailImage(item.thumbnail, Modifier.size(52.dp).clip(CircleShape))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Artist", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}

@Composable
fun AlbumListItem(item: AlbumItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThumbnailImage(item.thumbnail, Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            val artist = item.artists?.joinToString(", ") { it.name } ?: ""
            Text(if (artist.isNotBlank()) "Album • $artist" else "Album",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}

@Composable
fun PlaylistListItem(item: PlaylistItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThumbnailImage(item.thumbnail, Modifier.size(52.dp).clip(RoundedCornerShape(6.dp)))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Playlist • ${item.author?.name ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// SONG LIST ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SongListItem(song: SongItem, isPlaying: Boolean, onClick: () -> Unit) {
    val bg = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else Color.Transparent
    Row(
        modifier = Modifier.fillMaxWidth().background(bg).clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(52.dp)) {
            ThumbnailImage(song.thumbnail, Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)))
            if (isPlaying) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.VolumeUp, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        song.duration?.let { secs ->
            Text("%d:%02d".format(secs / 60, secs % 60),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onClick) {
            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play",
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
    HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
