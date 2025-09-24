package com.cinecraze.free;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.models.Season;
import com.cinecraze.free.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private Context context;
    private List<Entry> entryList;

    public CarouselAdapter(Context context, List<Entry> entryList) {
        this.context = context;
        this.entryList = entryList;
    }

    public void setEntries(List<Entry> entries) {
        this.entryList = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entryList.get(position);

        holder.title.setText(entry.getTitle());
        holder.year.setText(String.valueOf(entry.getYear()));
        holder.duration.setText(entry.getDuration());
        Glide.with(context).load(entry.getPoster()).into(holder.poster);
        
        holder.playButton.setOnClickListener(v -> {
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
                    Log.w("CarouselAdapter", "Large series detected with " + totalEpisodes + " episodes. Using lightweight data transfer.");
                    
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
                // The DetailsActivity will handle the entry data properly without needing all entries
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("CarouselAdapter", "Error starting DetailsActivity: " + e.getMessage(), e);
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
        TextView year;
        TextView duration;
        FloatingActionButton playButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            year = itemView.findViewById(R.id.year);
            duration = itemView.findViewById(R.id.duration);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
