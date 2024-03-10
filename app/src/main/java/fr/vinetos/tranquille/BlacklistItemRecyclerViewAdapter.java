package fr.vinetos.tranquille;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import fr.vinetos.tranquille.data.db.BlacklistItem;

public class BlacklistItemRecyclerViewAdapter extends GenericRecyclerViewAdapter
        <BlacklistItem, BlacklistItemRecyclerViewAdapter.ViewHolder> {

    private SelectionTracker<Long> selectionTracker;

    public BlacklistItemRecyclerViewAdapter(
            @Nullable ListInteractionListener<BlacklistItem> listener) {
        super(new DiffUtilCallback(), listener);
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    public ItemKeyProvider<Long> getItemKeyProvider() {
        return new ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {
            @Nullable
            @Override
            public Long getKey(int position) {
                BlacklistItem item = getItem(position);
                return item != null ? item.getId() : null;
            }

            @Override
            public int getPosition(@NonNull Long key) {
                for (int i = 0; i < getItemCount(); i++) {
                    BlacklistItem item = getItem(i);
                    if (item != null && key.equals(item.getId())) return i;
                }
                return RecyclerView.NO_POSITION;
            }
        };
    }

    public ItemDetailsLookup<Long> getItemDetailsLookup(RecyclerView recyclerView) {
        return new ItemDetailsLookup<Long>() {
            @Nullable
            @Override
            public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
                View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (view != null) {
                    RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
                    if (holder instanceof BlacklistItemRecyclerViewAdapter.ViewHolder) {
                        return ((BlacklistItemRecyclerViewAdapter.ViewHolder) holder).getItemDetails();
                    }
                }
                return null;
            }
        };
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blacklist_item, parent, false);
        return new BlacklistItemRecyclerViewAdapter.ViewHolder(view);
    }

    class ViewHolder extends GenericRecyclerViewAdapter
            <BlacklistItem, BlacklistItemRecyclerViewAdapter.ViewHolder>.GenericViewHolder {

        final TextView name, pattern, stats;
        final AppCompatImageView errorIcon;

        ItemDetailsLookup.ItemDetails<Long> itemDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            pattern = itemView.findViewById(R.id.pattern);
            stats = itemView.findViewById(R.id.stats);
            errorIcon = itemView.findViewById(R.id.errorIcon);
        }

        @Override
        void bind(BlacklistItem item) {
            if (item == null) { // placeholder
                name.setVisibility(View.INVISIBLE);
                pattern.setVisibility(View.INVISIBLE);
                stats.setVisibility(View.GONE);
                errorIcon.setVisibility(View.GONE);
                itemView.setActivated(false);

                return;
            }

            name.setText(item.getName());
            name.setVisibility(TextUtils.isEmpty(item.getName()) ? View.GONE : View.VISIBLE);

            pattern.setText(item.getHumanReadablePattern());
            pattern.setVisibility(View.VISIBLE);

            if (item.getNumberOfCalls() > 0) {
                stats.setVisibility(View.VISIBLE);

                Context context = stats.getContext();

                Date lastCallDate = item.getLastCallDate();
                String dateString = lastCallDate != null
                        ? DateUtils.getRelativeTimeSpanString(lastCallDate.getTime()).toString()
                        : context.getString(R.string.blacklist_item_date_no_info);

                stats.setText(context.getResources().getQuantityString(
                        R.plurals.blacklist_item_stats, item.getNumberOfCalls(),
                        item.getNumberOfCalls(), dateString));
            } else {
                stats.setVisibility(View.GONE);
            }

            errorIcon.setVisibility(item.getInvalid() ? View.VISIBLE : View.GONE);

            itemView.setActivated(selectionTracker != null
                    && selectionTracker.isSelected(item.getId()));
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            if (itemDetails == null) {
                itemDetails = new ItemDetailsLookup.ItemDetails<Long>() {
                    @Override
                    public int getPosition() {
                        return getAdapterPosition();
                    }

                    @Nullable
                    @Override
                    public Long getSelectionKey() {
                        int position = getAdapterPosition();
                        BlacklistItem item = position != RecyclerView.NO_POSITION
                                ? getItem(position) : null;
                        return item != null ? item.getId() : null;
                    }
                };
            }
            return itemDetails;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return super.toString() + " '" + pattern.getText() + "'";
        }

    }

    static class DiffUtilCallback extends DiffUtil.ItemCallback<BlacklistItem> {

        @Override
        public boolean areItemsTheSame(@NonNull BlacklistItem oldItem,
                                       @NonNull BlacklistItem newItem) {
            if (oldItem.getId() != null || newItem.getId() != null) {
                return ObjectsCompat.equals(oldItem.getId(), newItem.getId());
            }

            return ObjectsCompat.equals(oldItem.getPattern(), newItem.getPattern());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BlacklistItem oldItem,
                                          @NonNull BlacklistItem newItem) {
            return ObjectsCompat.equals(oldItem.getPattern(), newItem.getPattern())
                    && ObjectsCompat.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getNumberOfCalls() == newItem.getNumberOfCalls()
                    && ObjectsCompat.equals(oldItem.getLastCallDate(), newItem.getLastCallDate());
        }

    }

}
