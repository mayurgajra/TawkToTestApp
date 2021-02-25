package com.mayurg.tawktotestapp.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.shareddata.listOfUsersTest
import com.mayurg.tawktotestapp.testutils.MainCoroutineRule
import com.mayurg.tawktotestapp.testutils.createMockDataSourceFactory
import com.mayurg.tawktotestapp.testutils.getOrAwaitValueTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserListingRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainRule = MainCoroutineRule()

    private val githubAPI: GithubAPI = mock(GithubAPI::class.java)
    private val usersDao: UsersDao = mock(UsersDao::class.java)

    lateinit var userListingRepository: UserListingRepository

    @Before
    fun setUp() {
        userListingRepository = UserListingRepository(githubAPI, usersDao)
    }

    @Test
    fun test_LocalFetchedUsers() = runBlockingTest {

        Mockito.`when`(usersDao.getPagedUsers()).thenReturn(
            createMockDataSourceFactory(listOfUsersTest)
        )

        val users = userListingRepository.observeLocalPagedUsers().getOrAwaitValueTest()

        assertThat(users).isNotEmpty()
    }

    @Test
    fun test_LocalDataEmpty_ShouldCallApi() = runBlockingTest {
        Mockito.`when`(githubAPI.getUserListing("", 0)).thenReturn(
            Response.success(listOfUsersTest)
        )

        Mockito.`when`(usersDao.getPagedUsers()).thenReturn(
            createMockDataSourceFactory(listOf())
        )

        val users = userListingRepository.observeLocalPagedUsers().getOrAwaitValueTest()

        assertThat(users).isEmpty()
        verify(githubAPI).getUserListing("", 0)
    }

    @Test
    fun test_ApiSuccessStatus() = runBlockingTest {
        Mockito.`when`(githubAPI.getUserListing("", 0)).thenReturn(
            Response.success(listOfUsersTest)
        )
        val initialLoading = userListingRepository.isFetchInProgress.getOrAwaitValueTest()

        userListingRepository.fetchData()

        val isLoading = userListingRepository.isFetchInProgress.getOrAwaitValueTest()
        val error = userListingRepository.error.getOrAwaitValueTest()

        assertThat(isLoading).isFalse()
        assertThat(error).isEmpty()
    }

    @Test
    fun test_ApiFailureStatus() = runBlockingTest {

        val errorMsg = "Something went wrong"
        Mockito.`when`(githubAPI.getUserListing("", 0)).thenThrow(
            RuntimeException(errorMsg)
        )
        val initialLoading = userListingRepository.isFetchInProgress.getOrAwaitValueTest()

        userListingRepository.fetchData()

        val isLoading = userListingRepository.isFetchInProgress.getOrAwaitValueTest()
        val error = userListingRepository.error.getOrAwaitValueTest()

        assertThat(isLoading).isFalse()
        assertThat(error).isEqualTo(errorMsg)
    }

}