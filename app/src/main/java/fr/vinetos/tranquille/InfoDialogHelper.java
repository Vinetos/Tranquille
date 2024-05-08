package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import fr.vinetos.tranquille.data.NumberInfo;
import fr.vinetos.tranquille.data.SiaNumberCategoryUtils;
import fr.vinetos.tranquille.data.YacbHolder;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class InfoDialogHelper {

    public static void showDialog(Context context, NumberInfo numberInfo,
                                  DialogInterface.OnDismissListener onDismissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(!numberInfo.noNumber
                        ? numberInfo.number : context.getString(R.string.no_number));

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.info_dialog, null);
        builder.setView(view);

        TextView categoryView = view.findViewById(R.id.category);

        NumberCategory category = numberInfo.communityDatabaseItem != null
                ? NumberCategory.getById(numberInfo.communityDatabaseItem.getCategory())
                : null;

        if (category != null && category != NumberCategory.NONE) {
            categoryView.setText(SiaNumberCategoryUtils.getName(context, category));
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

        String blacklistName = null;

        TextView inBlacklistView = view.findViewById(R.id.in_blacklist);
        if (numberInfo.blacklistItem != null) {
            blacklistName = numberInfo.blacklistItem.getName();
            if (numberInfo.contactItem != null) {
                inBlacklistView.setText(R.string.info_in_blacklist_contact);
            }
        } else {
            inBlacklistView.setVisibility(View.GONE);
        }

        TextView blacklistNameView = view.findViewById(R.id.blacklist_name);
        if (!TextUtils.isEmpty(blacklistName)) {
            blacklistNameView.setText(blacklistName);
        } else {
            blacklistNameView.setVisibility(View.GONE);
        }

        ReviewsSummaryHelper.populateSummary(view.findViewById(R.id.reviews_summary),
                numberInfo.communityDatabaseItem);

        if (onDismissListener != null) builder.setOnDismissListener(onDismissListener);

        if (numberInfo.noNumber) {
            builder.show();
            return;
        }

        Runnable reviewsAction = () -> ReviewsActivity.startForNumber(context, numberInfo.number);

        Runnable webReviewAction = () -> {
            Uri uri = Uri.parse(YacbHolder.getWebService().getWebReviewsUrlPart()
                    + numberInfo.number);
            IntentHelper.startActivity(context, new Intent(Intent.ACTION_VIEW, uri));
        };

        Runnable addToBlacklistAction = () -> {
            FeaturedDatabaseItem featuredDatabaseItem = numberInfo.featuredDatabaseItem;
            String name = featuredDatabaseItem != null ? featuredDatabaseItem.getName() : null;
//            context.startActivity(EditBlacklistItemActivity
//                    .getIntent(context, name, numberInfo.number));
        };

        builder.setPositiveButton(R.string.add_web_review, null)
                .setNeutralButton(R.string.online_reviews, null)
                .setNegativeButton(R.string.add_to_blacklist, (dialog, which)
                        -> addToBlacklistAction.run());

        AlertDialog dialog = builder.create();

        // avoid dismissing the original dialog on button press

        dialog.setOnShowListener(x -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (numberInfo.contactItem != null) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.are_you_sure)
                            .setMessage(R.string.load_reviews_confirmation_message)
                            .setPositiveButton(R.string.yes, (d1, w) -> {
                                reviewsAction.run();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    reviewsAction.run();
                    dialog.dismiss();
                }
            });

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (numberInfo.contactItem != null) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.are_you_sure)
                            .setMessage(R.string.load_reviews_confirmation_message)
                            .setPositiveButton(R.string.yes, (d1, w) -> {
                                webReviewAction.run();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    webReviewAction.run();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

}
