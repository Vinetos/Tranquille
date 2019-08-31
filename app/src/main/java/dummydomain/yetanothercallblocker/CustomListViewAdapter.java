package dummydomain.yetanothercallblocker;

import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.CommunityReview;
import dummydomain.yetanothercallblocker.sia.model.NumberRating;

class CustomListViewAdapter extends RecyclerView.Adapter<CustomListViewAdapter.CommunityReviewViewHolder> {

    private List<CommunityReview> reviewsList = Collections.emptyList();

    public void setItems(List<CommunityReview> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public CommunityReviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.review_item, viewGroup, false);
        return new CommunityReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityReviewViewHolder viewHolder, int i) {
        viewHolder.bind(reviewsList.get(i));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    class CommunityReviewViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivRating;
        TextView tvNumberCategory, tvTitle, tvDescription;

        public CommunityReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRating = itemView.findViewById(R.id.rating_icon);
            tvNumberCategory = itemView.findViewById(R.id.number_category);
            tvTitle = itemView.findViewById(R.id.review_title);
            tvDescription = itemView.findViewById(R.id.review_comment);
        }

        public void bind(CommunityReview item) {
            tvNumberCategory.setText(item.getCategory().getStringId());
            String title = item.getTitle();
            if (TextUtils.isEmpty(title)) {
                tvTitle.setVisibility(View.GONE);
            } else {
                tvTitle.setText(title);
                tvTitle.setVisibility(View.VISIBLE);
            }
            String comment = item.getComment();
            if (TextUtils.isEmpty(comment)) {
                tvDescription.setVisibility(View.GONE);
            } else {
                tvDescription.setText(comment);
                tvDescription.setVisibility(View.VISIBLE);
            }

            IconAndColor iconAndColor = getRatingIconData(item.getRating());
            ivRating.setImageResource(iconAndColor.getIconResId());
            ImageViewCompat.setImageTintList(ivRating, ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(iconAndColor.getColorResId())));
        }

        protected IconAndColor getRatingIconData(NumberRating rating) {
            switch (rating) {
                case NEUTRAL:
                case UNKNOWN:
                    return IconAndColor.of(R.drawable.ic_thumbs_up_down_black_24dp, R.color.rateNeutral);
                case POSITIVE:
                    return IconAndColor.of(R.drawable.ic_thumb_up_black_24dp, R.color.ratePositive);
                case NEGATIVE:
                    return IconAndColor.of(R.drawable.ic_thumb_down_black_24dp, R.color.rateNegative);
            }
            return IconAndColor.of(R.drawable.ic_thumbs_up_down_black_24dp, R.color.notFound);
        }
    }
}
