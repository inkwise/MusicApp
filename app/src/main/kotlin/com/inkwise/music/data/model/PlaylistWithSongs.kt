package com.inkwise.music.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithSongs(

    @Embedded
    val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songs: List<Song>
)