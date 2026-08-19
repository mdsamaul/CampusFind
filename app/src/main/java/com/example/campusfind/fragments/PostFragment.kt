package com.example.campusfind.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.campusfind.databinding.FragmentPostBinding

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivItemPreview.setImageURI(selectedImageUri)
            binding.layoutUpload.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryDropdown()

        binding.cardImage.setOnClickListener {
            openGallery()
        }

        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Electronics", "ID Card", "Wallet", "Books", "Keys", "Others")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.autoCategory.setAdapter(adapter)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun validateAndSubmit() {
        val title = binding.etTitle.text.toString()
        val category = binding.autoCategory.text.toString()
        val location = binding.etLocation.text.toString()
        val type = if (binding.btnLost.isChecked) "Lost" else "Found"

        if (title.isEmpty() || category.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Firebase Logic
        Toast.makeText(requireContext(), "Posting $type Item: $title", Toast.LENGTH_LONG).show()
        
        // Switch back to Home Fragment
        // (This would normally be handled by a navigation controller or host activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
