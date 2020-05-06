package dummydomain.yetanothercallblocker;

import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.NumberRating;

class IconAndColor {

    @DrawableRes
    final int iconResId;
    @ColorRes
    final int colorResId;
    final boolean noInfo;

    private IconAndColor(int iconResId, int colorResId) {
        this(iconResId, colorResId, false);
    }

    private IconAndColor(int icon, int color, boolean noInfo) {
        this.iconResId = icon;
        this.colorResId = color;
        this.noInfo = noInfo;
    }

    void setOnImageView(AppCompatImageView imageView) {
        imageView.setImageResource(iconResId);
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(
                imageView.getContext().getResources().getColor(colorResId)));
    }

    static IconAndColor of(@DrawableRes int icon, @ColorRes int color) {
        return new IconAndColor(icon, color);
    }

    // TODO: fix duplication

    static IconAndColor forNumberRating(NumberRating rating) {
        switch (rating) {
            case NEUTRAL:
                return of(R.drawable.ic_thumbs_up_down_24dp, R.color.rateNeutral);
            case POSITIVE:
                return of(R.drawable.ic_thumb_up_24dp, R.color.ratePositive);
            case NEGATIVE:
                return of(R.drawable.ic_thumb_down_24dp, R.color.rateNegative);
        }
        return new IconAndColor(R.drawable.ic_thumbs_up_down_24dp, R.color.notFound, true);
    }

    static IconAndColor forNumberRating(NumberInfo.Rating rating) {
        switch (rating) {
            case NEUTRAL:
                return of(R.drawable.ic_thumbs_up_down_24dp, R.color.rateNeutral);
            case POSITIVE:
                return of(R.drawable.ic_thumb_up_24dp, R.color.ratePositive);
            case NEGATIVE:
                return of(R.drawable.ic_thumb_down_24dp, R.color.rateNegative);
        }
        return new IconAndColor(R.drawable.ic_thumbs_up_down_24dp, R.color.notFound, true);
    }
}
