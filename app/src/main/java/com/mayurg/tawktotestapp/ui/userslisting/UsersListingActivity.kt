package com.mayurg.tawktotestapp.ui.userslisting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mayurg.tawktotestapp.R
import com.mayurg.tawktotestapp.adapters.UsersListAdapter
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.databinding.ActivityUsersListingBinding
import com.mayurg.tawktotestapp.ui.BaseActivity
import com.mayurg.tawktotestapp.ui.userprofile.UserProfileActivity
import com.mayurg.tawktotestapp.utils.ConnectivityUtils
import com.mayurg.tawktotestapp.utils.Constants.ARG_LOGIN_ID
import com.mayurg.tawktotestapp.utils.CustomSearchViewListener
import com.mayurg.tawktotestapp.viewmodels.UsersListingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * UI to show the list of github users
 */
@AndroidEntryPoint
class UsersListingActivity : BaseActivity(), UsersListAdapter.OnItemClickListener {

    /**
     * View binding class autogenerated, provides access to the views of user list activity
     */
    private lateinit var binding: ActivityUsersListingBinding

    /**
     * [viewModel] is responsible to fetch and process the data provided by data repository
     */
    private val viewModel: UsersListingViewModel by viewModels()

    /**
     * Adapter class used to show Github users list in the recyclerview
     */
    private val usersListAdapter by lazy { UsersListAdapter() }

    companion object {
        /**
         * Delays the actual call for search by given milliseconds
         */
        const val SEARCH_DELAY = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
        setRvUserListing()
        observeViewModel()
        setSearchViewListeners()
        setSwipeRefreshListener()
    }

    /**
     * Binds the view of the activity and set the support actionbar
     */
    private fun bindView() {
        binding = ActivityUsersListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.usersListingToolbar)
    }

    /**
     * Sets the adapter click listener and initializes recyclerview
     * with adapter,linear layout manager and divider item decoration
     */
    private fun setRvUserListing() {
        usersListAdapter.onItemClickListener = this
        binding.rvUsersListing.apply {
            adapter = usersListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    /**
     * Observe the changes to viewmodel and updates ui accordingly
     */
    private fun observeViewModel() {
        viewModel.allUsers.observe(this, {
            usersListAdapter.submitList(it)
            hideLoading()
        })

        viewModel.isLoading.observe(this, {
            it?.let {
                if (it) showProgressBar() else hideLoading()
            }
        })

        viewModel.error.observe(this, {
            if (!it.isNullOrEmpty()) {
                if (!ConnectivityUtils.hasInternetConnection(this)) {
                    showSnackBar(
                        binding.root,
                        getString(R.string.no_internet_msg),
                        getString(R.string.got_it)
                    )
                } else {
                    showSnackBar(binding.root,it)
                }
            }
        })
    }

    /**
     * 1) Sets the listener for searchview query change and  updates the viewmodel to get the search result,
     * also debounce is added with coroutine so that it fetches data only when use has stopped typing for [SEARCH_DELAY]
     *
     * 2) Sets the search view close listener and updates the viewmodel to get the data without search queries
     */
    private fun setSearchViewListeners() {
        var job: Job? = null
        binding.searchView.setOnQueryTextListener(object : CustomSearchViewListener {
            override fun onQueryTextChange(newText: String): Boolean {
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY)
                    viewModel.searchQuery.value = newText
                }
                return false
            }
        })


        binding.searchView.setOnSearchViewListener(object : CustomSearchViewListener {
            override fun onSearchViewClosed() {
                viewModel.searchQuery.value = ""
            }
        })
    }

    /**
     * Sets the refresh listener and refreshes the data when users pulls down to refresh
     */
    private fun setSwipeRefreshListener() {
        binding.refreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    /**
     * Stops the loading of shimmer,refresh layout and progress bar
     */
    private fun hideLoading() {
        if (binding.rvUsersListing.visibility != View.VISIBLE) {
            binding.rvUsersListing.visibility = View.VISIBLE
        }
        binding.refreshLayout.isRefreshing = false
        binding.paginationProgressBar.visibility = View.GONE
        binding.shimmerLoadingUserListLayout.visibility = View.GONE
    }

    /**
     * Shows the progressbar or shimmer if the loading was not shown from refresh layout
     */
    private fun showProgressBar() {
        if (!binding.refreshLayout.isRefreshing) {
            if (usersListAdapter.itemCount > 0) {
                binding.paginationProgressBar.visibility = View.VISIBLE
            } else {
                binding.shimmerLoadingUserListLayout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Set the searchview menu item with [com.ferfalk.simplesearchview.SimpleSearchView]
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_user_listing, menu)
        val item: MenuItem = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Listens to the item click recyclerview and redirects to the [UserProfileActivity]
     */
    override fun onItemClick(
        item: ModelUserResponseItem,
        ivProfile: ImageView,
        tvLogin: TextView,
        position: Int
    ) {
        val pair1 = Pair.create(ivProfile as View, "profile")
        val pair2 = Pair.create(tvLogin as View, "username")
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            pair1,
            pair2
        )
        startActivity(
            Intent(this, UserProfileActivity::class.java).apply {
                putExtra(ARG_LOGIN_ID, item.login)
            },
            options.toBundle()
        )
    }

    /**
     * Used to refresh data when the internet available or show the message if internet not available
     */
    override fun onInternetChange(isAvailable: Boolean) {
        super.onInternetChange(isAvailable)
        if (isAvailable) {
            viewModel.refreshData()
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.no_internet_msg),
                getString(R.string.got_it)
            )
        }
    }

    /**
     * onBackPressed checks if searchview is open then closes it or exits the activity in not
     */
    override fun onBackPressed() {
        if (binding.searchView.isSearchOpen) {
            binding.searchView.closeSearch()
            return
        }
        super.onBackPressed()
    }

}