package dummydomain.yetanothercallblocker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class IntentHelper {

    public static Uri getUriForPhoneNumber(String number) {
        return Uri.parse("tel:" + (!TextUtils.isEmpty(number) ? number : "private"));
    }

    public static PendingIntent pendingActivity(Context context, Intent intent) {
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

}
