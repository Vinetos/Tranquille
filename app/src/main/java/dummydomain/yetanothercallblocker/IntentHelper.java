package dummydomain.yetanothercallblocker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(IntentHelper.class);

    public static Uri getUriForPhoneNumber(String number) {
        return Uri.parse("tel:" + (!TextUtils.isEmpty(number) ? number : "private"));
    }

    public static PendingIntent pendingActivity(Context context, Intent intent) {
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public static Intent clearTop(Intent intent) {
        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public static void startActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            LOG.warn("startActivity() error starting activity", e);
        }
    }

}
