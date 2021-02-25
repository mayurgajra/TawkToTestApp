package com.mayurg.tawktotestapp.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.paging.LimitOffsetDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.mayurg.tawktotestapp.testutils.getOrAwaitValue
import com.mayurg.tawktotestapp.shareddata.listOfUsersAndroidTest
import com.mayurg.tawktotestapp.shareddata.userAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UsersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: GitHubDatabase
    private lateinit var dao: UsersDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GitHubDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.getUsersListDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun test_insertUsersList() = runBlockingTest {
        dao.upsertUsers(listOfUsersAndroidTest)

        val factory = dao.getPagedUsers()
        val list = (factory.create() as LimitOffsetDataSource).loadRange(0, listOfUsersAndroidTest.size)

        assertThat(list.size).isEqualTo(listOfUsersAndroidTest.size)
    }

    @Test
    fun test_insertUser() = runBlockingTest {
        dao.upsertUser(userAndroidTest)

        val user = dao.getUserLiveData(userAndroidTest.login).getOrAwaitValue()

        assertThat(user.id).isEqualTo(userAndroidTest.id)
    }

    @Test
    fun test_saveNote() = runBlockingTest {
        dao.upsertUser(userAndroidTest)
        userAndroidTest.note = "Hello World"
        dao.saveNote(userAndroidTest.note ?: "", userAndroidTest.login)

        val note = dao.getNote(userAndroidTest.login)

        assertThat(note).isEqualTo(userAndroidTest.note)
    }

    @Test
    fun test_searchUsersByLogin() = runBlockingTest {
        dao.upsertUsers(listOfUsersAndroidTest)

        val factory = dao.searchUsers(userAndroidTest.login)
        val list = (factory.create() as LimitOffsetDataSource).loadRange(0, listOfUsersAndroidTest.size)

        assertThat(list.size).isGreaterThan(0)
    }

    @Test
    fun test_searchUsersByNote() = runBlockingTest {
        dao.upsertUsers(listOfUsersAndroidTest)

        val factory = dao.searchUsers(userAndroidTest.note ?: "")
        val list = (factory.create() as LimitOffsetDataSource).loadRange(0, listOfUsersAndroidTest.size)

        assertThat(list.size).isGreaterThan(0)
    }
}