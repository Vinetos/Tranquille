package dummydomain.yetanothercallblocker;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dummydomain.yetanothercallblocker.data.CallLogItem;

public class CallLogItemRecyclerViewAdapter
        extends RecyclerView.Adapter<CallLogItemRecyclerViewAdapter.ViewHolder> {

    public interface OnListInteractionListener {
        void onListFragmentInteraction(CallLogItem item);
    }

    private final List<CallLogItem> items;
    private final @Nullable OnListInteractionListener listener;

    public CallLogItemRecyclerViewAdapter(List<CallLogItem> items,
                                          @Nullable OnListInteractionListener listener) {
        this.items = items;
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
                case REJECTED:
                    icon = R.drawable.ic_call_missed_24dp;
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

            label.setText(item.numberInfo.name != null ? item.numberInfo.name : item.number);

            IconAndColor iconAndColor = IconAndColor.forNumberRating(
                    item.numberInfo.rating, item.numberInfo.contactItem != null);

            if (!iconAndColor.noInfo) {
                iconAndColor.setOnImageView(numberInfoIcon);
            } else {
                numberInfoIcon.setImageDrawable(null);
            }

            time.setText(DateUtils.getRelativeTimeSpanString(item.timestamp));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + label.getText() + "'";
        }
    }
}
