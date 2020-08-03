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

import java.util.List;

import dummydomain.yetanothercallblocker.data.CallLogItem;

public class CallLogItemRecyclerViewAdapter extends GenericRecyclerViewAdapter
        <CallLogItem, CallLogItemRecyclerViewAdapter.ViewHolder> {

    public CallLogItemRecyclerViewAdapter(@Nullable ListInteractionListener<CallLogItem> listener) {
        super(listener);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected DiffUtilCallback getDiffUtilCallback(
            List<CallLogItem> oldList, List<CallLogItem> newList) {
        return new DiffUtilCallback(oldList, newList);
    }

    class ViewHolder extends GenericRecyclerViewAdapter<CallLogItem, ViewHolder>.GenericViewHolder {

        final AppCompatImageView callTypeIcon;
        final TextView label;
        final AppCompatImageView numberInfoIcon;
        final TextView time;

        ViewHolder(View view) {
            super(view);

            callTypeIcon = view.findViewById(R.id.callTypeIcon);
            label = view.findViewById(R.id.item_label);
            numberInfoIcon = view.findViewById(R.id.numberInfoIcon);
            time = view.findViewById(R.id.time);
        }

        @Override
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
                    item.numberInfo.noNumber ? label.getContext().getString(R.string.no_number) :
                            item.numberInfo.name != null ? item.numberInfo.name : item.number,
                    15));

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

    static class DiffUtilCallback
            extends GenericRecyclerViewAdapter.GenericDiffUtilCallback<CallLogItem> {

        DiffUtilCallback(List<CallLogItem> oldList, List<CallLogItem> newList) {
            super(oldList, newList);
        }

        @Override
        protected boolean areItemsTheSame(CallLogItem oldItem, CallLogItem newItem) {
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

}
