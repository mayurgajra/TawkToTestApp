package com.mayurg.tawktotestapp.utils

import com.ferfalk.simplesearchview.SimpleSearchView

/**
 * Just a wrapper class for [SimpleSearchView] to avoid having to implement every method in activityl
 */
interface CustomSearchViewListener: SimpleSearchView.OnQueryTextListener,SimpleSearchView.SearchViewListener {

    override fun onQueryTextSubmit(query: String?): Boolean {
       return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
       return false
    }

    override fun onQueryTextCleared(): Boolean {
       return false
    }

    override fun onSearchViewShown() {

    }

    override fun onSearchViewClosed() {

    }

    override fun onSearchViewShownAnimation() {

    }

    override fun onSearchViewClosedAnimation() {

    }
}