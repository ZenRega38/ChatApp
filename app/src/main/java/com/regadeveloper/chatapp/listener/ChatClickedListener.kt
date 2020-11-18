package com.regadeveloper.chatapp.listener

interface ChatClickedListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatsImageUrl: String?, chatsName: String?)
}