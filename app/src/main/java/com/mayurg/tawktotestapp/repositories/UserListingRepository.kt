package com.mayurg.tawktotestapp.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.utils.Constants
import com.mayurg.tawktotestapp.utils.retryIO
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Repository class used to fetch the list of users from the [GithubAPI] and offline data source [UsersDao]
 * It first tries to get the from the offline db if it doesn't have data or reached the limit,
 * it will request the remote source for new data
 */
class UserListingRepository @Inject constructor(
    private val githubAPI: GithubAPI,
    private val usersDao: UsersDao,
) {

    companion object {
        /**
         * Number of items to load at once
         */
        private const val PAGE_SIZE = 30

        /**
         * Defines When the datasource should start fetching again when the recyclerview is
         * number of items away from the user's visibility
         */
        private const val PREFETCH_DISTANCE = 1

        /**
         * Configuration for the [LivePagedListBuilder] to build live data of PagedList
         */
        fun pagedListConfig() = PagedList.Config.Builder()
            .setInitialLoadSizeHint(PAGE_SIZE)
            .setPageSize(PAGE_SIZE)
            .setPrefetchDistance(PREFETCH_DISTANCE)
            .setEnablePlaceholders(false)
            .build()
    }

    /**
     * It is the user id that indicates from where api has to start fetching [e.g after id=30]
     */
    private var since = 0

    /**
     * Indicates whether the data is being loaded or not, to show loading indicators in ui
     * Other variable[isFetchInProgress] with just livedata is for outside classes to observe not mutate the state from outside
     */
    private val _isFetchInProgress = MutableLiveData<Boolean>().apply { postValue(false) }
    val isFetchInProgress: LiveData<Boolean> = _isFetchInProgress

    /**
     * Indicates whether there was any error in the fetch
     * Other variable[error] with just livedata is for outside classes to observe not mutate the state from outside
     */
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Parent job to keep track of jobs running in the [scope] and cancel when needed
     */
    private val parentJob = Job()

    /**
     * Coroutine scope to launch suspending network or database calls
     */
    private val scope = CoroutineScope(Dispatchers.IO + parentJob)

    /**
     * Used to observed paged list provided by the local db
     * @return [LiveData<PagedList<ModelUserResponseItem>>]
     */
    fun observeLocalPagedUsers(): LiveData<PagedList<ModelUserResponseItem>> {

        val factory = usersDao.getPagedUsers()

        return LivePagedListBuilder(
            factory,
            pagedListConfig()
        ).setBoundaryCallback(RepoBoundaryCallback()).build()
    }

    /**
     * Used to identify whether more data should be fetched or not
     */
    inner class RepoBoundaryCallback : PagedList.BoundaryCallback<ModelUserResponseItem>() {

        /**
         * Database returned 0 items. We should query the backend for more items.
         */
        override fun onZeroItemsLoaded() {
            requestAndSaveData()
        }

        /**
         * When all items in the database were loaded, we need to query the backend for more items.
         */
        override fun onItemAtEndLoaded(itemAtEnd: ModelUserResponseItem) {
            requestAndSaveData()
        }

        private fun requestAndSaveData() {
            if (isFetchInProgress.value == true) return

            fetchData()
        }
    }

    /**
     * Used to refresh data on swipe refresh or when the internet available
     */
    fun refresh() {
        since = 0
        fetchData()
    }

    /**
     * Fetches the data from github api and saves it in the database
     */
    fun fetchData() {
        scope.launch() {
            setLoading(true)
            try {
                val response = retryIO { githubAPI.getUserListing(Constants.auth, since) }
                if (response.isSuccessful) {
                    response.body()?.let { results ->
                        for (item in results) {
                            val existing = usersDao.getUser(item.login)
                            existing?.let {
                                item.note = existing.note
                                item.name = existing.name
                                item.company = existing.company
                                item.blog = existing.blog
                            }
                        }
                        usersDao.upsertUsers(results)
                        since = results.last().id
                    }
                    setLoading()
                    setError()
                } else {
                    setLoading()
                    setError("${response.code()} : ${response.message()}")
                }
            } catch (e: Exception) {
                setLoading()
                setError(e.message ?: e.toString())
            }
        }
    }

    /**
     * sets the loading and error value
     */
    private suspend fun setLoading(isLoading: Boolean = false) {
        withContext(Dispatchers.Main) {
            _isFetchInProgress.value = isLoading
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
     * Searches for the query provided by user on fields [login,note] and
     * @return [LiveData<PagedList<ModelUserResponseItem>>] instance for the classes to observe
     */
    fun searchUsers(query: String): LiveData<PagedList<ModelUserResponseItem>> {
        val dataSourceFactory = usersDao.searchUsers(query)

        return LivePagedListBuilder(
            dataSourceFactory,
            pagedListConfig()
        ).build()
    }

    /**
     * Cancels all the jobs
     */
    fun cancelAllJobs() {
        parentJob.cancel()
    }
}