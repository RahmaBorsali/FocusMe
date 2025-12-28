package com.example.focusme.presentation.screen.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(vm: MusicViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // ✅ handle audio focus
            )
            volume = 1f
        }
    }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    // release
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(state.currentTrack?.previewUrl) {
        val url = state.currentTrack?.previewUrl
        if (!url.isNullOrBlank()) {
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()

            if (state.isPlaying) player.play() else player.pause()
        }
        positionMs = 0L
        durationMs = 0L
    }


    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) player.play() else player.pause()
    }


    // poll progress
    LaunchedEffect(player) {
        while (true) {
            positionMs = player.currentPosition
            val d = player.duration
            durationMs = if (d > 0) d else 0L
            delay(300)
        }
    }

    val bg = Color.White
    val track = state.currentTrack

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(8.dp))

        // Artwork circle like screenshot
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            if (track?.artworkUrl100 != null) {
                Image(
                    painter = rememberAsyncImagePainter(track.artworkUrl100.replace("100x100", "300x300")),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)))
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = track?.trackName ?: "Song Title",
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = track?.artistName ?: "Artist Name",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))

        // progress slider
        val progress = if (durationMs == 0L) 0f else (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        Slider(
            value = progress,
            onValueChange = { p ->
                if (durationMs > 0) {
                    val seekTo = (durationMs * p).toLong()
                    player.seekTo(seekTo)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(positionMs), color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
            Text(formatMs(durationMs), color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        // controls row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { vm.prev() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(34.dp))
            }

            Spacer(Modifier.width(12.dp))

            FloatingActionButton(
                onClick = { vm.setPlaying(!state.isPlaying) },
                containerColor = Color(0xFF3B82F6) // bleu comme screenshot
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "PlayPause",
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(34.dp))
            }
        }

        Spacer(Modifier.height(18.dp))

        // little volume (just UI)
        Text("Volume", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
        Slider(
            value = player.volume,
            onValueChange = { v -> player.volume = v },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(0.75f)
        )

        Spacer(Modifier.height(10.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (state.error != null) {
            Text(state.error!!, color = Color(0xFFD32F2F))
        }
    }
}

private fun formatMs(ms: Long): String {
    val total = (ms / 1000).toInt().coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
