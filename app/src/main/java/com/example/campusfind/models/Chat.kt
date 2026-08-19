package com.example.campusfind.models

data class Chat(
    val id: String,
    val otherUserName: String,
    val itemTitle: String,
    val lastMessage: String,
    val time: String,
    val otherUserAvatar: String? = null
)

data class Message(
    val id: String,
    val text: String,
    val time: String,
    val isSentByMe: Boolean
)
