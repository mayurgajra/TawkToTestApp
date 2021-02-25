package com.mayurg.tawktotestapp.data.local

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem

/**
 * Data access object for the users table
 */
@Dao
interface UsersDao {

    /**
     * Insert the new list of users and replaces the new data on conflict
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsers(list: List<ModelUserResponseItem>)

    /**
     * Inserts the single user data and replaces the new data on conflict
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(item: ModelUserResponseItem)

    /**
     * Provides the livedata instance of [ModelUserResponseItem] for single user to observe changes
     * in the record
     */
    @Query("SELECT * FROM users WHERE login=:login")
    fun getUserLiveData(login: String): LiveData<ModelUserResponseItem>

    /**
     * Provides [DataSource.Factory] instance for PagedListBuilder to be used for pagination
     */
    @Query("SELECT * FROM users")
    fun getPagedUsers(): DataSource.Factory<Int, ModelUserResponseItem>

    /**
     * Saved the offline note inserted by user
     */
    @Query("UPDATE users SET note=:note WHERE login=:login")
    suspend fun saveNote(note: String, login: String)

    /**
     * Gets the offline saved note if any
     */
    @Query("SELECT note FROM users WHERE login=:login")
    suspend fun getNote(login: String): String

    /**
     * Searches for the query provided by user on fields [login,note] and
     * provides [DataSource.Factory] instance for PagedListBuilder to be used for pagination
     */
    @Query("SELECT * FROM users WHERE login LIKE '%' || :q || '%' OR note LIKE '%' || :q || '%'")
    fun searchUsers(q: String): DataSource.Factory<Int, ModelUserResponseItem>

    /**
     * Gets the single user by it's login id
     */
    @Query("SELECT * FROM users WHERE login=:login")
    suspend fun getUser(login: String): ModelUserResponseItem?
}