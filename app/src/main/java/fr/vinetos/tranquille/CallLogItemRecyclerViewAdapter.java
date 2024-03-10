package fr.vinetos.tranquille;

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
import androidx.recyclerview.widget.DiffUtil;

import java.util.Iterator;
import java.util.List;

import fr.vinetos.tranquille.data.CallLogItem;
import fr.vinetos.tranquille.data.CallLogItemGroup;
import fr.vinetos.tranquille.data.NumberInfo;

public class CallLogItemRecyclerViewAdapter extends GenericRecyclerViewAdapter
        <CallLogItemGroup, CallLogItemRecyclerViewAdapter.ViewHolder> {

    public CallLogItemRecyclerViewAdapter(@Nullable ListInteractionListener<CallLogItemGroup> listener) {
        super(new DiffUtilCallback(), listener);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_log_item, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends GenericRecyclerViewAdapter<CallLogItemGroup, ViewHolder>.GenericViewHolder {

        final AppCompatImageView[] callTypeIcons;
        final TextView label;
        final AppCompatImageView numberInfoIcon;
        final TextView duration;
        final TextView description;
        final TextView time;

        ViewHolder(View view) {
            super(view);

            callTypeIcons = new AppCompatImageView[]{
                    view.findViewById(R.id.callTypeIcon),
                    view.findViewById(R.id.callTypeIcon2),
                    view.findViewById(R.id.callTypeIcon3)
            };
            label = view.findViewById(R.id.item_label);
            numberInfoIcon = view.findViewById(R.id.numberInfoIcon);
            duration = view.findViewById(R.id.duration);
            description = view.findViewById(R.id.description);
            time = view.findViewById(R.id.time);
        }

        @Override
        void bind(CallLogItemGroup group) {
            if (group == null) { // placeholder
                label.setVisibility(View.INVISIBLE);
                numberInfoIcon.setVisibility(View.INVISIBLE);
                duration.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
                time.setVisibility(View.INVISIBLE);
                for (AppCompatImageView icon : callTypeIcons) {
                    bindTypeIcon(null, icon);
                }

                return;
            } else {
                label.setVisibility(View.VISIBLE);
                numberInfoIcon.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);
            }

            CallLogItem item = group.getItems().get(0);

            Context context = itemView.getContext();

            NumberInfo numberInfo = item.numberInfo;

            label.setText(getLabel(context, item));

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

            bindTypeIcons(group);

            String descriptionString = NumberInfoUtils.getShortDescription(context, numberInfo);
            if (!TextUtils.isEmpty(descriptionString)) {
                description.setText(descriptionString);
                description.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
            }

            CharSequence timeString = DateUtils.getRelativeTimeSpanString(
                    item.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_ALL);

            if (group.getItems().size() > 3) {
                timeString = "(" + group.getItems().size() + ") " + timeString;
            }

            time.setText(timeString);
        }

        private String getLabel(Context context, CallLogItem item) {
            NumberInfo numberInfo = item.numberInfo;

            if (numberInfo.noNumber) return context.getString(R.string.no_number);

            if (numberInfo.name != null) return numberInfo.name;

            if (numberInfo.blacklistItem != null
                    && !TextUtils.isEmpty(numberInfo.blacklistItem.getName())) {
                return numberInfo.blacklistItem.getName();
            }

            return item.number;
        }

        private void bindTypeIcons(CallLogItemGroup group) {
            List<CallLogItem> items = group.getItems();

            for (int i = 0; i < callTypeIcons.length; i++) {
                CallLogItem.Type type = i < items.size() ? items.get(i).type : null;
                bindTypeIcon(type, callTypeIcons[i]);
            }
        }

        private void bindTypeIcon(CallLogItem.Type type, AppCompatImageView view) {
            Integer icon = null;

            if (type != null) {
                switch (type) {
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
                }
            }

            if (icon != null) {
                view.setImageResource(icon);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
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

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return super.toString() + " '" + label.getText() + "'";
        }
    }

    static class DiffUtilCallback extends DiffUtil.ItemCallback<CallLogItemGroup> {

        @Override
        public boolean areItemsTheSame(@NonNull CallLogItemGroup oldGroup,
                                       @NonNull CallLogItemGroup newGroup) {
            if (oldGroup.getItems().size() != newGroup.getItems().size()) return false;

            for (Iterator<CallLogItem> it1 = oldGroup.getItems().iterator(),
                 it2 = newGroup.getItems().iterator(); it1.hasNext(); ) {
                if (!areItemsTheSame(it1.next(), it2.next())) return false;
            }

            return true;
        }

        protected boolean areItemsTheSame(CallLogItem oldItem, CallLogItem newItem) {
            return newItem.type == oldItem.type
                    && TextUtils.equals(newItem.number, oldItem.number)
                    && newItem.timestamp == oldItem.timestamp
                    && newItem.duration == oldItem.duration;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CallLogItemGroup oldItem,
                                          @NonNull CallLogItemGroup newItem) {
            return false; // time always updates
        }

    }

}
