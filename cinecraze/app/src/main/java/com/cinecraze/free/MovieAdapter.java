package com.cinecraze.free;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import com.bumptech.glide.Glide;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.models.Season;
import com.cinecraze.free.R;
import com.google.gson.Gson;

import java.util.List;
import android.util.Log;
import android.widget.Toast;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;
    private boolean isGridView;

    public MovieAdapter(Context context, List<Entry> entryList, boolean isGridView) {
        this.context = context;
        this.entryList = entryList;
        this.isGridView = isGridView;
    }

    public void setGridView(boolean gridView) {
        isGridView = gridView;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGridView) {
            view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        holder.title.setText(entry.getTitle());
        Glide.with(context).load(entry.getPoster()).into(holder.poster);
        holder.rating.setRating(entry.getRating());

        if (holder.description != null) {
            holder.description.setText(entry.getDescription());
        }
        if (holder.year != null) {
            holder.year.setText(String.valueOf(entry.getYear()));
        }
        if (holder.duration != null) {
            holder.duration.setText(entry.getDuration());
        }
        
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, DetailsActivity.class);
                
                // Check if entry has too many episodes (potential TransactionTooLargeException)
                int totalEpisodes = 0;
                if (entry.getSeasons() != null) {
                    for (Season season : entry.getSeasons()) {
                        if (season.getEpisodes() != null) {
                            totalEpisodes += season.getEpisodes().size();
                        }
                    }
                }
                
                // If too many episodes, pass only essential data to avoid TransactionTooLargeException
                if (totalEpisodes > 500) { // Conservative limit
                    Log.w("MovieAdapter", "Large series detected with " + totalEpisodes + " episodes. Using lightweight data transfer.");
                    
                    // Create a lightweight entry with only essential data
                    Entry lightweightEntry = new Entry();
                    lightweightEntry.setTitle(entry.getTitle());
                    lightweightEntry.setDescription(entry.getDescription());
                    lightweightEntry.setPoster(entry.getPoster());
                    lightweightEntry.setThumbnail(entry.getThumbnail());
                    lightweightEntry.setRating(entry.getRating());
                    lightweightEntry.setDuration(entry.getDuration());
                    lightweightEntry.setYear(entry.getYear());
                    lightweightEntry.setServers(entry.getServers());
                    // Don't set seasons - will be loaded separately in DetailsActivity
                    
                    intent.putExtra("entry", new Gson().toJson(lightweightEntry));
                    intent.putExtra("needsFullData", true);
                    intent.putExtra("entryId", entry.getId());
                } else {
                    // For smaller series, pass the full entry
                    intent.putExtra("entry", new Gson().toJson(entry));
                }
                
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("MovieAdapter", "Error starting DetailsActivity: " + e.getMessage(), e);
                Toast.makeText(context, "Error opening content details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar rating;
        TextView description;
        TextView year;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            rating = itemView.findViewById(R.id.rating);
            description = itemView.findViewById(R.id.description);
            year = itemView.findViewById(R.id.year);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}
