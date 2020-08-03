package dummydomain.yetanothercallblocker;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class GenericRecyclerViewAdapter<T, V extends GenericRecyclerViewAdapter<T, V>.GenericViewHolder>
        extends RecyclerView.Adapter<V> {

    public interface ListInteractionListener<T> {
        void onListItemClicked(T item);
    }

    protected @Nullable
    ListInteractionListener<T> listener;

    protected List<T> items = Collections.emptyList();

    public GenericRecyclerViewAdapter(@Nullable ListInteractionListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        onBindViewHolder(holder, items.get(position));
    }

    protected void onBindViewHolder(@NonNull V holder, T item) {
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<T> items) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                getDiffUtilCallback(this.items, items));

        this.items = items;

        diffResult.dispatchUpdatesTo(this);
    }

    protected abstract GenericDiffUtilCallback<T> getDiffUtilCallback(
            List<T> oldList, List<T> newList);

    protected void onClick(int index) {
        if (index != RecyclerView.NO_POSITION && listener != null) {
            listener.onListItemClicked(items.get(index));
        }
    }

    protected static class GenericDiffUtilCallback<T> extends DiffUtil.Callback {
        protected List<T> oldList;
        protected List<T> newList;

        protected GenericDiffUtilCallback(List<T> oldList, List<T> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return areItemsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
        }

        protected boolean areItemsTheSame(T oldItem, T newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
        }

        protected boolean areContentsTheSame(T oldItem, T newItem) {
            return false;
        }
    }

    protected abstract class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(v -> onClick(getAdapterPosition()));
        }

        abstract void bind(T item);
    }

}
