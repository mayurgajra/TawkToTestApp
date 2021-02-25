package com.mayurg.tawktotestapp.ui.userprofile

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.mayurg.tawktotestapp.R
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.data.local.UsersDao
import com.mayurg.tawktotestapp.data.remote.GithubAPI
import com.mayurg.tawktotestapp.repositories.UserProfileRepository
import com.mayurg.tawktotestapp.shareddata.userAndroidTest
import com.mayurg.tawktotestapp.utils.Constants
import com.mayurg.tawktotestapp.viewmodels.UserProfileViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class UserProfileActivityTest {

    var intent: Intent? = null

    init {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val login = userAndroidTest.login
        intent = Intent(targetContext, UserProfileActivity::class.java).apply {
            putExtra(Constants.ARG_LOGIN_ID, login)
        }
    }

    @get:Rule
    val activityRule = ActivityScenarioRule<UserProfileActivity>(intent)

    private val githubAPI: GithubAPI = mock(GithubAPI::class.java)
    private val usersDao: UsersDao = mock(UsersDao::class.java)

    lateinit var userProfileRepository: UserProfileRepository
    private val fakeLiveData: LiveData<ModelUserResponseItem> = MutableLiveData(userAndroidTest)

    private lateinit var userProfileViewModel: UserProfileViewModel

    @Before
    fun setUp() {
        userProfileRepository = UserProfileRepository(githubAPI, usersDao)
        userProfileViewModel = UserProfileViewModel(userProfileRepository)

        val login = userAndroidTest.login
        `when`(userProfileRepository.getUser(login)).thenReturn(
            fakeLiveData
        )
    }

    @Test
    fun checkNameValue() {
        onView(withId(R.id.tvName)).check(matches(withText(userAndroidTest.name)))
    }

    @Test
    fun checkCompanyValue() {
        onView(withId(R.id.tvCompany)).check(matches(withText(userAndroidTest.company)))
    }

    @Test
    fun checkBlogValue() {
        onView(withId(R.id.tvBlog)).check(matches(withText(userAndroidTest.blog)))
    }

    @Test
    fun checkFollowersValue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val followers = context.getString(R.string.followers, userAndroidTest.followers)
        onView(withId(R.id.tvFollowers)).check(matches(withText(followers)))
    }

    @Test
    fun checkFollowingValue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val following = context.getString(R.string.following, userAndroidTest.following)
        onView(withId(R.id.tvFollowing)).check(matches(withText(following)))
    }

    @Test
    fun checkSnackBarDisplayedOnSave() {
        onView(withId(R.id.btnSave)).perform(click())

        onView(withText(R.string.note_saved)).check(matches(isDisplayed()))
    }


}