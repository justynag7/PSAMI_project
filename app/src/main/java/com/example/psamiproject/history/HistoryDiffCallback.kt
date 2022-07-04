package com.example.psamiproject.history

import androidx.recyclerview.widget.DiffUtil
import com.example.psamiproject.data.UserActivity

class HistoryDiffCallback : DiffUtil.ItemCallback<UserActivity>() {
    override fun areItemsTheSame(oldItem: UserActivity, newItem: UserActivity): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UserActivity, newItem: UserActivity): Boolean {
        return oldItem == newItem
    }
}