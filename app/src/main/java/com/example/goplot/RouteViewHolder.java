package com.example.goplot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RouteViewHolder extends RecyclerView.ViewHolder {

    private final TextView routeItemView;

    public RouteViewHolder(@NonNull View itemView) {
        super(itemView);
        routeItemView = itemView.findViewById(R.id.textView);
    }

    static RouteViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new RouteViewHolder(view);
    }

    // RouteListAdapter sets the Route object's human-friendly String with this method
    public void bind(String text) {
        routeItemView.setText(text);
    }
}
