package com.inkwise.music.ui.main.navigationPage.local

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.inkwise.music.R
import com.inkwise.music.data.model.Song

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    /*
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
    )
    */
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
    )
    {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .then(
                            if (isPlaying) {
                                Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArt)
                        .size(128)
                        .precision(Precision.INEXACT)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Crop,
                )


            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    color =
                        if (isPlaying) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vert),
                        contentDescription = "音质",
                        //tint = animatedThemeColor,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPlaying) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                    )

                    if (!song.album.isNullOrBlank()) {
                        Text(
                            text = " - ${song.album}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPlaying) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                        )
                    }

                }

            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "菜单",
                    tint = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "菜单",
                    tint = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun AudioQualityIcon(
    sampleRate: Int,
    bitDepth: Int,
    bitrate: Int
) {
    // 直接在 Compose 里判断音质
    val (iconRes, color) = when {
        bitDepth >= 24 && sampleRate >= 96000 -> R.drawable.ic_more_vert to Color.Red   // HR
        bitDepth >= 16 && sampleRate >= 44100 -> R.drawable.ic_more_vert to Color.Blue  // FLAC
        bitrate >= 320_000 -> R.drawable.ic_more_vert to Color.Green                     // HQ
        else -> R.drawable.ic_more_vert to Color.Gray                                    // SQ
    }

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = "音质",
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}