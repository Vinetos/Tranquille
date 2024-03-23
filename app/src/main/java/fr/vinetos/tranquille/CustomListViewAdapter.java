package fr.vinetos.tranquille;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import fr.vinetos.tranquille.data.SiaNumberCategoryUtils;
import dummydomain.yetanothercallblocker.sia.model.CommunityReview;

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
            tvNumberCategory.setText(SiaNumberCategoryUtils.getNameResId(item.getCategory()));
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

            IconAndColor.forReviewRating(item.getRating()).applyToImageView(ivRating);
        }
    }
}
