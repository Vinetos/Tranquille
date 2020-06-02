package dummydomain.yetanothercallblocker;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

public class UiUtils {

    @ColorInt
    public static int getColorInt(@NonNull Context context, @ColorRes int colorResId) {
        return ResourcesCompat.getColor(context.getResources(), colorResId, context.getTheme());
    }

}
