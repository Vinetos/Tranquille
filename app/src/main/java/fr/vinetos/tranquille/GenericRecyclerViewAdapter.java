package fr.vinetos.tranquille;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GenericRecyclerViewAdapter<T, V extends GenericRecyclerViewAdapter<T, V>.GenericViewHolder>
        extends PagedListAdapter<T, V> {

    public interface ListInteractionListener<T> {
        void onListItemClicked(T item);
    }

    protected @Nullable ListInteractionListener<T> listener;

    public GenericRecyclerViewAdapter(DiffUtil.ItemCallback<T> itemCallback,
                                      @Nullable ListInteractionListener<T> listener) {
        super(itemCallback);
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        onBindViewHolder(holder, getItem(position));
    }

    protected void onBindViewHolder(@NonNull V holder, T item) {
        holder.bind(item);
    }

    protected void onClick(int index) {
        if (index != RecyclerView.NO_POSITION && listener != null) {
            T item = getItem(index);
            if (item != null) {
                listener.onListItemClicked(item);
            }
        }
    }

    protected abstract class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> onClick(getBindingAdapterPosition()));
        }

        abstract void bind(T item);
    }

}
