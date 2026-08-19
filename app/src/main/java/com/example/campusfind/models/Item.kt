package com.example.campusfind.models

data class Item(
    val id: String,
    val title: String,
    val category: String,
    val type: String, // "Lost" or "Found"
    val location: String,
    val time: String,
    val imageUrl: String? = null
)
