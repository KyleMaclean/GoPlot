package com.example.goplot;

import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class RouteListAdapter extends ListAdapter<Route, RouteViewHolder> {

    public RouteListAdapter(@NonNull DiffUtil.ItemCallback<Route> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return RouteViewHolder.create(parent);
    }

    // this holder is used by every RecyclerView in the app, so the user can always view the details of a Route consistently
    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route current = getItem(position);
        holder.bind(current.getRoute());
        // the user taps on a Route so we launch the SingleRouteActivity with the Route's ID
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SingleRouteActivity.class);
            intent.putExtra("routeId", current.get_id());
            v.getContext().startActivity(intent);
        });
    }

    // best practice to provide a difference checker for my custom POJO
    public static class RouteDiff extends DiffUtil.ItemCallback<Route> {

        @Override
        public boolean areItemsTheSame(@NonNull Route oldItem, @NonNull Route newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Route oldItem, @NonNull Route newItem) {
            return oldItem.getRoute().equals(newItem.getRoute());
        }
    }
}
