package com.mayurg.tawktotestapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.repositories.UserListingRepository
import com.mayurg.tawktotestapp.shareddata.listOfUsersTest
import com.mayurg.tawktotestapp.testutils.MainCoroutineRule
import com.mayurg.tawktotestapp.testutils.createMockDataSourceFactory
import com.mayurg.tawktotestapp.testutils.getOrAwaitValueTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UsersListingViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainRule = MainCoroutineRule()

    private val githubAPI: GithubAPI = mock(GithubAPI::class.java)
    private val usersDao: UsersDao = mock(UsersDao::class.java)

    lateinit var userListingRepository: UserListingRepository
    lateinit var usersListingViewModel: UsersListingViewModel

    @Before
    fun setUp() {
        userListingRepository = UserListingRepository(githubAPI, usersDao)
        usersListingViewModel = UsersListingViewModel(userListingRepository)
    }

    @Test
    fun test_providesAllUsers_WhenSearchQueryEmpty() {
        Mockito.`when`(usersDao.getPagedUsers()).thenReturn(
            createMockDataSourceFactory(listOfUsersTest)
        )

        val users = usersListingViewModel.allUsers.getOrAwaitValueTest()

        assertThat(users.size).isEqualTo(listOfUsersTest.size)
    }

    @Test
    fun test_providesSearchUsers_WhenSearchQueryProvided() {
        val searchTerm = "moj"
        val searchList =
            listOfUsersTest.filter { it.login.contains(searchTerm) || it.note?.contains(searchTerm) == true }

        usersListingViewModel.searchQuery.postValue(searchTerm)
        Mockito.`when`(usersDao.searchUsers(searchTerm)).thenReturn(
            createMockDataSourceFactory(searchList)
        )

        val users = usersListingViewModel.allUsers.getOrAwaitValueTest()

        assertThat(users.size).isEqualTo(searchList.size)
    }

}