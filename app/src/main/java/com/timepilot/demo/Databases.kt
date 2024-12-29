package com.timepilot.demo

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(entities = [Event::class, AllowedApp::class, BlockedWebsites::class, AllowedWebsites::class], version = 1)
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

    @Upsert
    suspend fun insertApp(allowedApp: AllowedApp)

    @Query("SELECT * FROM AllowedApp WHERE eventId = :eventId")
    suspend fun getAllowedApps(eventId: Int): List<AllowedApp>

    @Upsert
    suspend fun insertBlockedWeb(allowedApp: AllowedApp)

    @Query("SELECT * FROM BlockedWebsites WHERE eventId = :eventId")
    suspend fun getBlockedWebsites(eventId: Int): List<BlockedWebsites>

    @Upsert
    suspend fun insertAllowedWeb(allowedApp: AllowedApp)

    @Query("SELECT * FROM AllowedWebsites WHERE eventId = :eventId")
    suspend fun getAllowedWebsites(eventId: Int): List<AllowedWebsites>
}