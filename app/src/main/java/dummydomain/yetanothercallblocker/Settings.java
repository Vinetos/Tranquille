package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String PREF_INCOMING_CALL_NOTIFICATIONS = "incomingCallNotifications";
    private static final String PREF_BLOCK_CALLS = "blockCalls";

    private final SharedPreferences pref;

    Settings(Context context) {
        pref = context.getSharedPreferences("yacb_preferences", Context.MODE_PRIVATE);
    }

    public boolean getIncomingCallNotifications() {
        return getBoolean(PREF_INCOMING_CALL_NOTIFICATIONS, true);
    }

    public void setIncomingCallNotifications(boolean show) {
        setBoolean(PREF_INCOMING_CALL_NOTIFICATIONS, show);
    }

    public boolean getBlockCalls() {
        return getBoolean(PREF_BLOCK_CALLS);
    }

    public void setBlockCalls(boolean block) {
        setBoolean(PREF_BLOCK_CALLS, block);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    private void setBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

}
