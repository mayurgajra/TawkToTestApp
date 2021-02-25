package com.mayurg.tawktotestapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mayurg.tawktotestapp.R
import com.mayurg.tawktotestapp.data.entities.ModelUserResponseItem
import com.mayurg.tawktotestapp.databinding.ListItemUsersBinding
import com.mayurg.tawktotestapp.utils.loadImage
import com.mayurg.tawktotestapp.utils.loadImageInverted
import com.mayurg.tawktotestapp.utils.setSafeText

/**
 * Adapter class used to show Github users list in [com.mayurg.tawktotestapp.ui.userslisting.UsersListingActivity]
 */
class UsersListAdapter :
    PagedListAdapter<ModelUserResponseItem, UsersListAdapter.UserViewHolder>(DiffUtilsCallBack()) {

    /**
     * Used to listen to item clicks of this adapter
     */
    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(
            item: ModelUserResponseItem,
            ivProfile: ImageView,
            tvLogin: TextView,
            position: Int
        )
    }

    /**
     * Used to provide the difference between items for efficient loading
     */
    class DiffUtilsCallBack : DiffUtil.ItemCallback<ModelUserResponseItem>() {
        override fun areItemsTheSame(
            oldItem: ModelUserResponseItem,
            newItem: ModelUserResponseItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ModelUserResponseItem,
            newItem: ModelUserResponseItem
        ): Boolean {
            return oldItem == newItem
        }
    }


    /**
     * ViewHolder class of single user item
     * Binds the data with the given view
     */
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ListItemUsersBinding.bind(view)
        fun bindView(user: ModelUserResponseItem?, onItemClickListener: OnItemClickListener?) {
            user?.let {
                binding.apply {
                    if (adapterPosition % 4 == 0) {
                        ivUserProfile.loadImageInverted(user.avatarUrl)
                    } else {
                        ivUserProfile.loadImage(user.avatarUrl)
                    }
                    tvUserName.setSafeText(user.login)
                    val userIdString = "User Id: ${user.id}"
                    tvUserId.setSafeText(userIdString)
                    root.setOnClickListener {
                        onItemClickListener?.onItemClick(user, ivUserProfile,tvUserName,adapterPosition)
                    }

                    ivNotes.visibility = if (user.note.isNullOrEmpty()) View.GONE else View.VISIBLE

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_users, parent, false)
    )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindView(getItem(position), onItemClickListener)
    }


}