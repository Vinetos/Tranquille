package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

import dummydomain.yetanothercallblocker.data.db.BlacklistItem;

public class BlacklistItemRecyclerViewAdapter extends GenericRecyclerViewAdapter
        <BlacklistItem, BlacklistItemRecyclerViewAdapter.ViewHolder> {

    private SelectionTracker<Long> selectionTracker;

    public BlacklistItemRecyclerViewAdapter(
            @Nullable ListInteractionListener<BlacklistItem> listener) {
        super(listener);
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    public ItemKeyProvider<Long> getItemKeyProvider() {
        return new ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {
            @Nullable
            @Override
            public Long getKey(int position) {
                return items.get(position).getId();
            }

            @Override
            public int getPosition(@NonNull Long key) {
                for (int i = 0; i < items.size(); i++) {
                    BlacklistItem item = items.get(i);
                    if (key.equals(item.getId())) return i;
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

    @Override
    protected DiffUtilCallback getDiffUtilCallback(
            List<BlacklistItem> oldList, List<BlacklistItem> newList) {
        return new DiffUtilCallback(oldList, newList);
    }

    class ViewHolder extends GenericRecyclerViewAdapter
            <BlacklistItem, BlacklistItemRecyclerViewAdapter.ViewHolder>.GenericViewHolder {

        final TextView name, pattern, stats;
        final AppCompatImageView errorIcon;
        final DateFormat dateFormat, timeFormat;

        ItemDetailsLookup.ItemDetails<Long> itemDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            pattern = itemView.findViewById(R.id.pattern);
            stats = itemView.findViewById(R.id.stats);
            errorIcon = itemView.findViewById(R.id.errorIcon);

            dateFormat = android.text.format.DateFormat.getMediumDateFormat(itemView.getContext());
            timeFormat = android.text.format.DateFormat.getTimeFormat(itemView.getContext());
        }

        @Override
        void bind(BlacklistItem item) {
            name.setText(item.getName());
            name.setVisibility(TextUtils.isEmpty(item.getName()) ? View.GONE : View.VISIBLE);

            pattern.setText(item.getHumanReadablePattern());

            if (item.getNumberOfCalls() > 0) {
                stats.setVisibility(View.VISIBLE);

                Context context = stats.getContext();
                String dateString = item.getLastCallDate() != null
                        ? dateFormat.format(item.getLastCallDate()) + ' '
                        + timeFormat.format(item.getLastCallDate())
                        : context.getString(R.string.blacklist_item_date_no_info);

                stats.setText(context.getResources().getQuantityString(
                        R.plurals.blacklist_item_stats, item.getNumberOfCalls(),
                        item.getNumberOfCalls(), dateString));
            } else {
                stats.setVisibility(View.GONE);
            }

            errorIcon.setVisibility(item.getInvalid() ? View.VISIBLE : View.GONE);

            if (selectionTracker != null) {
                itemView.setActivated(selectionTracker.isSelected(item.getId()));
            }
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
                        return position != RecyclerView.NO_POSITION
                                ? items.get(position).getId() : null;
                    }
                };
            }
            return itemDetails;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + pattern.getText() + "'";
        }

    }

    static class DiffUtilCallback
            extends GenericRecyclerViewAdapter.GenericDiffUtilCallback<BlacklistItem> {

        DiffUtilCallback(List<BlacklistItem> oldList, List<BlacklistItem> newList) {
            super(oldList, newList);
        }

        @Override
        protected boolean areItemsTheSame(BlacklistItem oldItem, BlacklistItem newItem) {
            if (oldItem.getId() != null || newItem.getId() != null) {
                return ObjectsCompat.equals(oldItem.getId(), newItem.getId());
            }

            return ObjectsCompat.equals(oldItem.getPattern(), newItem.getPattern());
        }

        @Override
        protected boolean areContentsTheSame(BlacklistItem oldItem, BlacklistItem newItem) {
            return ObjectsCompat.equals(oldItem.getPattern(), newItem.getPattern())
                    && ObjectsCompat.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getNumberOfCalls() == newItem.getNumberOfCalls()
                    && ObjectsCompat.equals(oldItem.getLastCallDate(), newItem.getLastCallDate());
        }

    }

}
