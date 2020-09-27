package dummydomain.yetanothercallblocker;

import android.content.Context;
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
import dummydomain.yetanothercallblocker.data.NumberInfo;

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
        final TextView duration;
        final TextView description;
        final TextView time;

        ViewHolder(View view) {
            super(view);

            callTypeIcon = view.findViewById(R.id.callTypeIcon);
            label = view.findViewById(R.id.item_label);
            numberInfoIcon = view.findViewById(R.id.numberInfoIcon);
            duration = view.findViewById(R.id.duration);
            description = view.findViewById(R.id.description);
            time = view.findViewById(R.id.time);
        }

        @Override
        void bind(CallLogItem item) {
            Context context = itemView.getContext();

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

            NumberInfo numberInfo = item.numberInfo;

            label.setText(numberInfo.noNumber
                    ? context.getString(R.string.no_number)
                    : numberInfo.name != null ? numberInfo.name : item.number);

            IconAndColor iconAndColor = IconAndColor.forNumberRating(
                    numberInfo.rating, numberInfo.contactItem != null);

            if (!iconAndColor.noInfo) {
                iconAndColor.applyToImageView(numberInfoIcon);
            } else {
                numberInfoIcon.setImageDrawable(null);
            }

            if (item.duration == 0 && item.type == CallLogItem.Type.MISSED
                    || item.type == CallLogItem.Type.REJECTED) {
                duration.setVisibility(View.GONE);
            } else {
                duration.setText(getDuration(context, item.duration));
                duration.setVisibility(View.VISIBLE);
            }

            String descriptionString = NumberInfoUtils.getShortDescription(context, numberInfo);
            if (!TextUtils.isEmpty(descriptionString)) {
                description.setText(descriptionString);
                description.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
            }

            time.setText(DateUtils.getRelativeTimeSpanString(
                    item.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_ALL));
        }

        private String getDuration(Context context, long duration) {
            long seconds = duration % 60;
            long minutes = duration / 60;
            long hours = minutes / 60;
            minutes -= hours * 60;

            if (hours != 0) {
                return context.getString(R.string.duration_h_m_s, hours, minutes, seconds);
            } else if (minutes != 0) {
                return context.getString(R.string.duration_m_s, minutes, seconds);
            }
            return context.getString(R.string.duration_s, seconds);
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
