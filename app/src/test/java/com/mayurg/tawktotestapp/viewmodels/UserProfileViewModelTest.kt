package com.mayurg.tawktotestapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.repositories.UserProfileRepository
import com.mayurg.tawktotestapp.shareddata.userTest
import com.mayurg.tawktotestapp.testutils.MainCoroutineRule
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
class UserProfileViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainRule = MainCoroutineRule()

    private val githubAPI: GithubAPI = mock(GithubAPI::class.java)
    private val usersDao: UsersDao = mock(UsersDao::class.java)

    lateinit var userProfileRepository: UserProfileRepository
    private val fakeLiveData: LiveData<ModelUserResponseItem> = MutableLiveData(userTest)

    private lateinit var userProfileViewModel: UserProfileViewModel

    @Before
    fun setUp() {
        userProfileRepository = UserProfileRepository(githubAPI, usersDao)
        userProfileViewModel = UserProfileViewModel(userProfileRepository)
    }

    @Test
    fun test_ReturnProfileIfExists(){
        val login =  userTest.login
        userProfileViewModel.login = login
        Mockito.`when`(userProfileRepository.getUser(login)).thenReturn(
            fakeLiveData
        )

        val user = userProfileViewModel.user.getOrAwaitValueTest()

        assertThat(user.id).isEqualTo(userTest.id)
    }

}