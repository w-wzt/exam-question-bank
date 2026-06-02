package com.example.examquestionbank.data.local.converter

import androidx.room.TypeConverter
import com.example.examquestionbank.domain.model.OptionItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromOptionItemList(value: List<OptionItem>): String = gson.toJson(value)

    @TypeConverter
    fun toOptionItemList(value: String): List<OptionItem> {
        if (value.isBlank()) return emptyList()
        val type = object : TypeToken<List<OptionItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String = gson.toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        if (value.isBlank()) return emptyList()
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        if (value.isBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntStringListMap(value: Map<Int, List<String>>): String = gson.toJson(value)

    @TypeConverter
    fun toIntStringListMap(value: String): Map<Int, List<String>> {
        if (value.isBlank()) return emptyMap()
        val type = object : TypeToken<Map<Int, List<String>>>() {}.type
        return gson.fromJson(value, type)
    }
}
