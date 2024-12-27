package com.timepilot.demo

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Event::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class Databases: RoomDatabase() {
    abstract val dao: EventDao
}