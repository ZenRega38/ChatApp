package com.regadeveloper.chatapp.data

import android.content.Context
import android.widget.ImageView
import com.regadeveloper.chatapp.R
import de.hdodenhof.circleimageview.CircleImageView

data class User(
    val email: String? = "",
    val phone: String? = "",
    val name: String? = "",
    val imageUrl: String? = "",
    val status: String? = "",
    val statusUrl: String? = "",
    val statusTime: String? = ""
)

data class Contact(
    val name: String?,
    val phone: String?
)

data class Message(
    val sentBy: String? = "",
    val message: String? = "",
    val messageTime: Long? = 0
)


