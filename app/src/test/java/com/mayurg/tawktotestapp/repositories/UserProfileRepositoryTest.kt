package com.mayurg.tawktotestapp.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.shareddata.userTest
import com.mayurg.tawktotestapp.testutils.MainCoroutineRule
import com.mayurg.tawktotestapp.testutils.getOrAwaitValueTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserProfileRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainRule = MainCoroutineRule()

    private val githubAPI: GithubAPI = mock(GithubAPI::class.java)
    private val usersDao: UsersDao = mock(UsersDao::class.java)

    lateinit var userProfileRepository: UserProfileRepository
    private val fakeLiveData: LiveData<ModelUserResponseItem> = MutableLiveData(userTest)


    @Before
    fun setUp() {
        userProfileRepository = UserProfileRepository(githubAPI, usersDao)
    }

    @Test
    fun test_profileFromLocalDb() {

        val login = userTest.login

        Mockito.`when`(usersDao.getUserLiveData(userTest.login)).thenReturn(
            fakeLiveData
        )

        val user = userProfileRepository.getUser(login).getOrAwaitValueTest()

        assertThat(user.name).isEqualTo(userTest.name)
    }

    @Test
    fun test_refreshDataSuccess() = runBlockingTest {

        val login = userTest.login

        Mockito.`when`(githubAPI.getUser("", login)).thenReturn(
            Response.success(userTest)
        )

        Mockito.`when`(usersDao.getUserLiveData(login)).thenReturn(
            fakeLiveData
        )

        userProfileRepository.refreshData(login)

        val user = userProfileRepository.getUser(login).getOrAwaitValueTest()
        val error = userProfileRepository.error.getOrAwaitValueTest()

        assertThat(user.id).isEqualTo(userTest.id)
        assertThat(error).isEmpty()
    }

    @Test
    fun test_refreshDataFailure() = runBlockingTest {

        val login = userTest.login
        val errorMsg = "Something went wrong"

        Mockito.`when`(githubAPI.getUser("", login)).thenThrow(
            RuntimeException(errorMsg)
        )

        userProfileRepository.refreshData(login)

        val error = userProfileRepository.error.getOrAwaitValueTest()

        assertThat(error).isEqualTo(errorMsg)
    }
}