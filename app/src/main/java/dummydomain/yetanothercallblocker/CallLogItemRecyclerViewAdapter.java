package dummydomain.yetanothercallblocker;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dummydomain.yetanothercallblocker.data.CallLogItem;

public class CallLogItemRecyclerViewAdapter
        extends RecyclerView.Adapter<CallLogItemRecyclerViewAdapter.ViewHolder> {

    public interface OnListInteractionListener {
        void onListFragmentInteraction(CallLogItem item);
    }

    private static class DiffUtilCallback extends DiffUtil.Callback {
        private List<CallLogItem> oldList;
        private List<CallLogItem> newList;

        DiffUtilCallback(List<CallLogItem> oldList, List<CallLogItem> newList) {
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
            CallLogItem oldItem = oldList.get(oldItemPosition);
            CallLogItem newItem = newList.get(newItemPosition);

            return newItem.type == oldItem.type
                    && TextUtils.equals(newItem.number, oldItem.number)
                    && newItem.timestamp == oldItem.timestamp
                    && newItem.duration == oldItem.duration;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return false; // time always updates
        }
    }

    private final @Nullable OnListInteractionListener listener;

    private List<CallLogItem> items = Collections.emptyList();

    public CallLogItemRecyclerViewAdapter(@Nullable OnListInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<CallLogItem> items) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DiffUtilCallback(this.items, items));

        this.items = items;

        diffResult.dispatchUpdatesTo(this);
    }

    private void onClick(int index) {
        if (index != RecyclerView.NO_POSITION && listener != null) {
            listener.onListFragmentInteraction(items.get(index));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;

        final AppCompatImageView callTypeIcon;
        final TextView label;
        final AppCompatImageView numberInfoIcon;
        final TextView time;

        ViewHolder(View view) {
            super(view);

            this.view = view;

            callTypeIcon = view.findViewById(R.id.callTypeIcon);
            label = view.findViewById(R.id.item_label);
            numberInfoIcon = view.findViewById(R.id.numberInfoIcon);
            time = view.findViewById(R.id.time);

            view.setOnClickListener(v -> onClick(getAdapterPosition()));
        }

        void bind(CallLogItem item) {
            Integer icon;
            switch (item.type) {
                case INCOMING:
                    icon = R.drawable.ic_call_received_24dp;
                    break;

                case OUTGOING:
                    icon = R.drawable.ic_call_made_24dp;
                    break;

                case MISSED:
                    icon = R.drawable.ic_call_missed_24dp;
                    break;

                case REJECTED:
                    icon = R.drawable.ic_call_rejected_24dp;
                    break;

                default:
                    icon = null;
                    break;
            }
            if (icon != null) {
                callTypeIcon.setImageResource(icon);
            } else {
                callTypeIcon.setImageDrawable(null);
            }

            label.setText(ellipsize(
                    item.numberInfo.name != null ? item.numberInfo.name : item.number, 15));

            IconAndColor iconAndColor = IconAndColor.forNumberRating(
                    item.numberInfo.rating, item.numberInfo.contactItem != null);

            if (!iconAndColor.noInfo) {
                iconAndColor.setOnImageView(numberInfoIcon);
            } else {
                numberInfoIcon.setImageDrawable(null);
            }

            time.setText(DateUtils.getRelativeTimeSpanString(item.timestamp));
        }

        String ellipsize(String s, int maxLength) {
            return s == null || s.length() <= maxLength
                    ? s
                    : (s.substring(0, maxLength - 1) + '…');
        }

        @Override
        public String toString() {
            return super.toString() + " '" + label.getText() + "'";
        }
    }
}
