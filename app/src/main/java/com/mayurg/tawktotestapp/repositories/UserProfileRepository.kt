package com.mayurg.tawktotestapp.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.utils.Constants
import com.mayurg.tawktotestapp.utils.retryIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository class used to fetch the profile of user from the [GithubAPI] and offline data source [UsersDao]
 * It first tries to get th data from the offline db and in parallel makes call to fetch the updated data
 */
class UserProfileRepository @Inject constructor(
    private val githubAPI: GithubAPI,
    private val usersDao: UsersDao,
) {


    /**
     * Indicates whether there was any error in the fetch
     * Other variable[error] with just livedata is for outside classes to observe not mutate the state from outside
     */
    private val _error = MutableLiveData<String>().apply { postValue("") }
    val error: LiveData<String> = _error

    /**
     * Used to provide the livedata object of user based on the id given
     *
     * @param login is user login id
     *
     * @return LiveData<ModelUserResponseItem>
     */
    fun getUser(login: String): LiveData<ModelUserResponseItem> {
        return usersDao.getUserLiveData(login)
    }

    /**
     * Used to refresh data on swipe refresh or when the internet available
     *
     * @param login is user login id
     */
    suspend fun refreshData(login: String) {
        try {
            val response = retryIO { githubAPI.getUser(Constants.auth, login) }
            if (response.isSuccessful) {
                response.body()?.let {
                    it.note = usersDao.getNote(login)
                    usersDao.upsertUser(it)
                }
            } else {
                setError("${response.code()} : ${response.message()}")
            }
        } catch (e: Exception) {
            setError(e.message ?: e.toString())
        }
    }

    /**
     * sets the error value
     */
    private suspend fun setError(error: String = "") {
        withContext(Dispatchers.Main) {
            _error.value = error
        }
    }


    /**
     * Saves the note added by user in the local database
     *
     * @param login is user login id
     * @param note added by the user
     */
    suspend fun saveNote(note: String, login: String) {
        usersDao.saveNote(note, login)
    }

}