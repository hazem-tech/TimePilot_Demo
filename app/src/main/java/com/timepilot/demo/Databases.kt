package com.timepilot.demo

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(entities = [Event::class], version = 1)
@TypeConverters(Converters::class)
abstract class Databases: RoomDatabase() {
    abstract val dao: EventDao
}

@Dao
interface EventDao {
    @Upsert
    suspend fun upsertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM event WHERE date = :specificDate ORDER BY position")
    fun getEvents(specificDate: String): Flow<List<Event>>
}