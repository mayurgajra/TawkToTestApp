package com.mayurg.tawktotestapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayurg.tawktotestapp.repositories.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel responsible for managing data for the profile activity
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    /**
     * Selected user's login id
     */
    var login = ""

    /**
     * Livedata object of a user being fetched from [userProfileRepository],
     * lazy initalizes allows us to set variable needed before we start listening for changes
     */
    val user by lazy { userProfileRepository.getUser(login) }

    /**
     * Indicates whether there was any error in the fetch
     */
    val error = userProfileRepository.error

    /**
     * Saves the note added by user in the local database
     *
     * @param login is user login id
     * @param note added by the user
     */
    fun saveNote(note: String, login: String) = viewModelScope.launch(Dispatchers.IO) {
        userProfileRepository.saveNote(note, login)
    }

    /**
     * Used to refresh data on swipe refresh or when the internet available
     */
    fun refreshData() = viewModelScope.launch(Dispatchers.IO) {
        userProfileRepository.refreshData(login)
    }


}