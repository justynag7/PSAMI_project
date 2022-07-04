package com.example.psamiproject.data

import java.text.SimpleDateFormat
import java.util.*

data class UserActivity(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val date: Long = 0
) {

    fun stringDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyy HH:mm", Locale.ENGLISH)
        return sdf.format(Date(date))

    }
}
