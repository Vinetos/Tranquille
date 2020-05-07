package dummydomain.yetanothercallblocker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PendingIntentHelper {

    public static PendingIntent forActivity(Context context, Intent intent) {
        // creates a new pending intent instead of returning an existing one
        return PendingIntent.getActivity(context, getRandomInt(), intent, 0);
    }

    private static int getRandomInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ThreadLocalRandom.current().nextInt();
        } else {
            return new Random().nextInt();
        }
    }

}
