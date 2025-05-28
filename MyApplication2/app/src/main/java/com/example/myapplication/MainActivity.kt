package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.TableInfo
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme



//Функуия для форматирования времени

fun formatDuration(seconds: Int?): String {
    return if (seconds != null && seconds > 0) {
        val minutes = seconds / 60
        val secs = seconds % 60
        String.format("%d:%02d", minutes, secs) // Формат "минуты:секунды"
    } else {
        "хх:хх"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF121212)
            ) {
                TrackListScreen()
            }
        }
    }
}

@Composable
fun Disign(title: String, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1E3A8A))
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Популярная музыка",
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Bold
                    )
                )
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Найти",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
    }

@Composable
fun TrackListScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var headerTitle by remember { mutableStateOf("Загрузка...") }

    var showSearchDialog by remember { mutableStateOf(false) }
    var trackInput by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Загрузка треков из Room
            val roomTracks = db.trackDao().getAllTracks().mapNotNull { entity ->
                try {
                    Track(
                        title = entity.title,
                        duration = entity.duration?.toIntOrNull(),
                        album = Album(
                            title = entity.albumTitle,
                            coverMedium = entity.albumCoverMedium
                        ),
                        artist = Artist(
                            name = entity.artistName,
                            pictureMedium = entity.artistPictureMedium
                        )
                    )
                } catch (e: Exception) {
                    Log.e("TrackListScreen", "Ошибка маппинга трека из Room: ${e.message}")
                    null
                }
            }
            if (roomTracks.isNotEmpty()) {
                tracks = roomTracks
                headerTitle = "Загрузка из базы данных"
                Log.d("TrackListScreen", "Загружено ${roomTracks.size} треков из Room")
            } else {
                headerTitle = "Нет данных в базе"
                Log.d("TrackListScreen", "В базе Room нет треков")
            }

            // Пытка загрузки треков из API
            try {
                val response = RetrofitInstance.api.getTopTracks()
                if (response.isSuccessful) {
                    val apiTracks = response.body()?.data ?: emptyList()
                    Log.d("TrackListScreen", "Треки из API: $apiTracks")

                    if (apiTracks.isNotEmpty()) {
                        headerTitle = "Загрузка данных из API"
                        tracks = apiTracks
                        // Сохранение треков из API в Room
                        db.trackDao().deleteAll()
                        db.trackDao().insertAll(
                            apiTracks.map {
                                TrackEntity(
                                    title = it.title,
                                    duration = it.duration?.toString() ?: "хх.хх",
                                    albumTitle = it.album?.title,
                                    albumCoverMedium = it.album?.coverMedium,
                                    artistName = it.artist?.name,
                                    artistPictureMedium = it.artist?.pictureMedium
                                )
                            }
                        )
                        apiTracks.forEach { track ->
                            Log.d("TrackListScreen", "Название трека из API: ${track.title}")
                        }
                    } else if (roomTracks.isEmpty()) {
                        errorMessage = "API вернул пустой список, и в базе нет данных"
                        headerTitle = "Нет данных"
                        Log.d("TrackListScreen", "API вернул пустой список, и Room пуст")
                    }
                } else {
                    errorMessage = "Ошибка API: ${response.code()}"
                    Log.w("TrackListScreen", "Ошибка API: ${response.code()}")
                    if (roomTracks.isEmpty()) {
                        headerTitle = "Нет данных"
                    }
                }
            } catch (e: Exception) {

                Log.e("TrackListScreen", "Исключение: ${e.message}", e)
                if (roomTracks.isEmpty()) {
                    headerTitle = "Нет данных"
                }
            }
        }
    }

    // Диалог поиска
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Введите название трека", color = Color(0xFFE0E0E0)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = trackInput,
                        onValueChange = { trackInput = it },
                        label = { Text("Название трека", color = Color(0xFFB0B0B0)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color(0xFFE0E0E0))
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSearchDialog = false
                        val results = tracks.filter { it.title?.contains(trackInput, ignoreCase = true) ?: false }
                        searchResults = results
                        showResultDialog = true
                        Log.d("TrackListScreen", "Результаты поиска: $results")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSearchDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) {
                    Text("Отмена")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE0E0E0),
            textContentColor = Color(0xFFB0B0B0)
        )
    }

    // Диалог результатов поиска
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Результаты поиска", color = Color(0xFFE0E0E0)) },
            text = {
                if (searchResults.isEmpty()) {
                    Text("Треков не найдено", color = Color(0xFFB0B0B0))
                } else {
                    LazyColumn {
                        items(searchResults) { track ->
                            Text(
                                text = "${track.title ?: "Без названия"} - ${formatDuration(track.duration)}",
                                color = Color(0xFFE0E0E0),
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showResultDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                ) {
                    Text("Закрыть")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE0E0E0),
            textContentColor = Color(0xFFB0B0B0)
        )
    }

    // Основной пользовательский интерфейс
    Column(modifier = modifier) {
        Disign(
            title = headerTitle,
            onButtonClick = { showSearchDialog = true }
        )
        // Сообщение об ошибке
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color(0xFFEF5350),
                modifier = Modifier.padding(16.dp),
                style = TextStyle(fontSize = 16.sp)
            )
        }
        // Список треков, если они есть
        if (tracks.isNotEmpty()) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(tracks) { track ->
                    TrackItem(track = track)
                }
            }
        } else if (errorMessage == null) {
            // Загрузка
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
fun TrackItem(track: Track) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(170.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = track.album?.coverMedium,
                contentDescription = " ${track.album?.title}",
                modifier = Modifier
                    .size(150.dp)
                    .padding(end = 16.dp)
            )
            Column {
                Text(
                    text = track.title ?: "Название не найденo",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color(0xFFE0E0E0)
                    )
                )
                Text(
                    text = "Длительность: ${formatDuration(track.duration)}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                )
                Text(
                    text = "Альбом: ${track.album?.title ?: "Альбом не найден"}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                )
                Text(
                    text = "${track.artist?.name ?: "Исполнитель не найден"}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0B0)
                    )
                )
                AsyncImage(
                    model = track.artist?.pictureMedium,
                    contentDescription = "Album cover for ${track.artist?.name}",
                    modifier = Modifier
                        .size(50.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Disign(title = "≈", onButtonClick = {})
        TrackListScreen()
    }
}