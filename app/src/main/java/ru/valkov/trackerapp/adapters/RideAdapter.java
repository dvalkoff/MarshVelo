package ru.valkov.trackerapp.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import ru.valkov.trackerapp.database.Ride;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder>{
    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class RideViewHolder extends RecyclerView.ViewHolder {

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private DiffUtil.ItemCallback<Ride> diffCallback = new DiffUtil.ItemCallback<Ride>() {
        @Override
        public boolean areItemsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return false;
        }
    };

}
