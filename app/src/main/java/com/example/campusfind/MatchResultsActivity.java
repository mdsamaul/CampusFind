package com.example.campusfind;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.MatchAdapter;
import com.example.campusfind.databinding.ActivityMatchResultsBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class MatchResultsActivity extends AppCompatActivity {

    private ActivityMatchResultsBinding binding;
    private FirebaseFirestore db;
    private List<Item> matchList;
    private MatchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        matchList = new ArrayList<>();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();

        String currentId = getIntent().getStringExtra("itemId");
        String category = getIntent().getStringExtra("category");
        String type = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");

        if (currentId != null) {
            performSmartMatch(currentId, category, type, title);
        }
    }

    private void setupRecyclerView() {
        adapter = new MatchAdapter(matchList, false);
        binding.rvMatchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatchResults.setAdapter(adapter);
    }

    private void performSmartMatch(String currentId, String category, String type, String title) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Match opposite type: if current is Lost, search for Found
        String targetType = "Lost".equals(type) ? "Found" : "Lost";

        db.collection("items")
                .whereEqualTo("category", category)
                .whereEqualTo("type", targetType)
                .whereEqualTo("status", "Active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    matchList.clear();
                    
                    String[] words = title.toLowerCase().split("\\s+");

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getId().equals(currentId)) continue;
                        
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            
                            // Simple word-based similarity check
                            boolean isSimilar = false;
                            String itemTitle = item.getTitle().toLowerCase();
                            for (String word : words) {
                                if (word.length() > 2 && itemTitle.contains(word)) {
                                    isSimilar = true;
                                    break;
                                }
                            }
                            
                            if (isSimilar) {
                                matchList.add(item);
                            }
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (matchList.isEmpty()) {
                        binding.tvNoMatches.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoMatches.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
