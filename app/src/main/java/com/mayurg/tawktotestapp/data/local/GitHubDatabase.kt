package com.mayurg.tawktotestapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem

/**
 * Main room database class containing references to entities and DAOs
 */
@Database(entities = [ModelUserResponseItem::class], version = 1, exportSchema = false)
abstract class GitHubDatabase : RoomDatabase() {
    abstract fun getUsersListDao(): UsersDao
}