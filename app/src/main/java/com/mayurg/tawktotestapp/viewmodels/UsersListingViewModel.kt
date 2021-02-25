package com.mayurg.tawktotestapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.repositories.UserListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Viewmodel responsible for managing data for the user's list activity
 */
@HiltViewModel
class UsersListingViewModel @Inject constructor(
    private val userListingRepository: UserListingRepository,
) : ViewModel() {

    /**
     * Updates the livedata being observed if there is a change in search by a user
     */
    val searchQuery = MutableLiveData<String>().apply { postValue("") }

    /**
     * Livedata object of a list of users being fetched from [userListingRepository]
     * Updates based of searchQuery
     */
    lateinit var allUsers: LiveData<PagedList<ModelUserResponseItem>>

    /**
     * Indicates whether the data is being loaded or not, to show loading indicators in ui
     */
    val isLoading = userListingRepository.isFetchInProgress

    /**
     * Indicates whether there was any error in the fetch
     */
    val error = userListingRepository.error


    init {
        getCurrentLiveData()
    }

    /**
     * Updates the current livedata object being observed in the activity based on the search query
     */
    private fun getCurrentLiveData() {
        allUsers = Transformations.switchMap(searchQuery) { input ->
            if (input.isNotEmpty()) {
                userListingRepository.searchUsers(input)
            } else {
                userListingRepository.observeLocalPagedUsers()
            }
        }
    }


    /**
     * Used to refresh data on swipe refresh or when the internet available
     */
    fun refreshData() {
        userListingRepository.refresh()
    }

    /**
     * Cancels jobs being performed when the viewmodel ie being destroyed
     */
    override fun onCleared() {
        super.onCleared()
        userListingRepository.cancelAllJobs()
    }

}