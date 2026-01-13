package com.canopus.Vmusic.data.db

import androidx.room.TypeConverter
import com.canopus.Vmusic.data.model.HolodexSong
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HolodexSongListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromHolodexSongList(songs: List<HolodexSong>?): String? {
        return songs?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toHolodexSongList(songsJson: String?): List<HolodexSong>? {
        return songsJson?.let {
            val listType = object : TypeToken<List<HolodexSong>>() {}.type
            gson.fromJson(it, listType)
        }
    }
}