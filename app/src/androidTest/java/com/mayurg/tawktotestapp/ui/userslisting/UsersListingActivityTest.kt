package com.mayurg.tawktotestapp.ui.userslisting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.mayurg.tawktotestapp.R
import com.mayurg.tawktotestapp.adapters.UsersListAdapter
import com.mayurg.tawktotestapp.ui.userprofile.UserProfileActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class UsersListingActivityTest {


    @get:Rule
    val activityRule = ActivityScenarioRule(UsersListingActivity::class.java)

    @Test
    fun checkToolbarTitle() {
        onView(withId(R.id.usersListingToolbar)).check(matches(hasDescendant(withText(R.string.users))))
    }

    @Test
    fun checkShimmerLoadingIsVisibleOnLaunch() {
        onView(withId(R.id.shimmerLoadingUserListLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun clickOnItemShouldOpenProfile() {
        Intents.init()
        onView(withId(R.id.rvUsersListing)).perform(
            RecyclerViewActions.actionOnItemAtPosition<UsersListAdapter.UserViewHolder>(
                0,
                click()
            )
        )

        Intents.intended(hasComponent(UserProfileActivity::class.java.name))
        Intents.release()
    }


}