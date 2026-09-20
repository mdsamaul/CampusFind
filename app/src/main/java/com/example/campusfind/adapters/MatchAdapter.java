package com.example.campusfind.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusfind.databinding.ItemMatchCardBinding;
import com.example.campusfind.models.Item;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private final List<Item> matches;
    private final boolean isHorizontal;

    public MatchAdapter(List<Item> matches, boolean isHorizontal) {
        this.matches = matches;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchCardBinding binding = ItemMatchCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (!isHorizontal) {
            // Adjust width for vertical list
            binding.getRoot().getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            // Add some vertical margin
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.getRoot().getLayoutParams();
            params.setMargins(40, 20, 40, 20);
        }
        return new MatchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Item item = matches.get(position);
        holder.binding.tvMatchTitle.setText(item.getTitle());
        holder.binding.tvMatchLocation.setText("Location: " + item.getLocation());
        
        // Dummy confidence score for AI UI demo
        String confidence = (90 - position * 5) + "% AI Match";
        holder.binding.tvMatchConfidence.setText(confidence);

        holder.binding.btnConfirmMatch.setOnClickListener(v -> {
            // TODO: Logic for user to confirm this match
            Toast.makeText(v.getContext(), "Verification request sent for: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        final ItemMatchCardBinding binding;

        public MatchViewHolder(ItemMatchCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
