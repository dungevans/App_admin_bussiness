package com.lethanh.ql_com_dao_bk.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lethanh.ql_com_dao_bk.R;

import java.util.List;
import java.util.function.BiConsumer;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {

    private final List<T> items;
    private final BiConsumer<T, ViewHolder> binder;

    public GenericAdapter(List<T> items, BiConsumer<T, ViewHolder> binder) {
        this.items = items;
        this.binder = binder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        binder.accept(items.get(position), holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title, subtitle;
        public final ImageView image;
        public final ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
            image = itemView.findViewById(R.id.iv_image);
            deleteButton = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}