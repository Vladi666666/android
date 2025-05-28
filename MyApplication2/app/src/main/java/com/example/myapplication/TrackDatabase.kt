package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room

// Сущность для трека
@Entity
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String?,
    val duration: String?,
    val albumTitle: String?,
    val albumCoverMedium: String?,
    val artistName: String?,
    val artistPictureMedium: String?
)
// DAO для работы с базой
@Dao
interface TrackDao {
    @Insert
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("SELECT * FROM TrackEntity")
    suspend fun getAllTracks(): List<TrackEntity>

    @Query("DELETE FROM TrackEntity")
    suspend fun deleteAll()
}

// БД
@Database(entities = [TrackEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "track_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}