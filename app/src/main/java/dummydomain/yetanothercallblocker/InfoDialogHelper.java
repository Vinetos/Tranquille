package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;

public class InfoDialogHelper {

    public static void showDialog(Context context, NumberInfo numberInfo,
                                  DialogInterface.OnDismissListener onDismissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(numberInfo.number);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.info_dialog, null);

        TextView categoryView = view.findViewById(R.id.category);

        NumberCategory category = numberInfo.communityDatabaseItem != null
                ? NumberCategory.getById(numberInfo.communityDatabaseItem.getCategory())
                : null;

        if (category != null && category != NumberCategory.NONE) {
            categoryView.setText(NumberCategory.getString(context, category));
        } else {
            categoryView.setVisibility(View.GONE);
        }

        TextView nameView = view.findViewById(R.id.name);

        String contactName = numberInfo.contactItem != null
                ? numberInfo.contactItem.displayName : null;

        if (!TextUtils.isEmpty(contactName)) {
            nameView.setText(contactName);
        } else {
            nameView.setVisibility(View.GONE);
        }

        TextView featuredNameView = view.findViewById(R.id.featured_name);

        String featuredName = numberInfo.featuredDatabaseItem != null
                ? numberInfo.featuredDatabaseItem.getName() : null;

        if (!TextUtils.isEmpty(featuredName)) {
            featuredNameView.setText(featuredName);
        } else {
            featuredNameView.setVisibility(View.GONE);
        }

        ReviewsSummaryHelper.populateSummary(view.findViewById(R.id.reviews_summary),
                numberInfo.communityDatabaseItem);

        builder.setView(view)
                .setNeutralButton(R.string.online_reviews, null)
                .setNegativeButton(R.string.back, null);

        if (onDismissListener != null) builder.setOnDismissListener(onDismissListener);

        AlertDialog dialog = builder.create();

        // avoid dismissing the original dialog on button press

        Runnable action = () -> {
            ReviewsActivity.startForNumber(context, numberInfo.number);
            dialog.dismiss();
        };

        dialog.setOnShowListener(x -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (numberInfo.contactItem != null) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.load_reviews_confirmation_title)
                            .setMessage(R.string.load_reviews_confirmation_message)
                            .setPositiveButton(android.R.string.yes, (d1, w) -> action.run())
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                } else {
                    action.run();
                }
            });
        });

        dialog.show();
    }

}
