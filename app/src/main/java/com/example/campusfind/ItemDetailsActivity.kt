package com.example.campusfind

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusfind.databinding.ActivityItemDetailsBinding

class ItemDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Load Dummy Data
        displayDummyData()

        // Button Click Listeners
        binding.btnStartChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_NAME", binding.tvPosterName.text.toString())
            intent.putExtra("ITEM_TITLE", binding.tvDetailTitle.text.toString())
            startActivity(intent)
        }

        binding.btnMarkResolved.setOnClickListener {
            // TODO: Firebase logic to mark item as resolved
            Toast.makeText(this, "Item marked as Resolved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayDummyData() {
        // These would normally come from Intent extras or Firestore
        binding.tvDetailTitle.text = "Scientific Calculator"
        binding.chipDetailCategory.text = "Electronics"
        binding.tvDetailLocation.text = "Science Lab, Room 302"
        binding.tvDetailDate.text = "Oct 28, 2023"
        binding.tvDetailDescription.text = "I found a Casio scientific calculator in the physics lab yesterday afternoon. It has a small sticker on the back."
        binding.chipDetailType.text = "Found"
        binding.tvPosterName.text = "Jane Smith"

        // Mock Owner Logic: If current user ID == owner UID
        val isOwner = false // Dummy condition
        if (isOwner) {
            binding.btnStartChat.visibility = View.GONE
            binding.btnMarkResolved.visibility = View.VISIBLE
        } else {
            binding.btnStartChat.visibility = View.VISIBLE
            binding.btnMarkResolved.visibility = View.GONE
        }
    }
}
