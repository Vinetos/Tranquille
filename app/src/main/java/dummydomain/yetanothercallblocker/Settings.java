package dummydomain.yetanothercallblocker;

import android.content.Context;

public class Settings extends GenericSettings {

    private static final String PREF_INCOMING_CALL_NOTIFICATIONS = "incomingCallNotifications";
    private static final String PREF_BLOCK_CALLS = "blockCalls";
    private static final String PREF_USE_CONTACTS = "useContacts";
    private static final String PREF_LAST_UPDATE_TIME = "lastUpdateTime";
    private static final String PREF_LAST_UPDATE_CHECK_TIME = "lastUpdateCheckTime";

    Settings(Context context) {
        super(context, "yacb_preferences");
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

    public boolean getUseContacts() {
        return getBoolean(PREF_USE_CONTACTS);
    }

    public void setUseContacts(boolean use) {
        setBoolean(PREF_USE_CONTACTS, use);
    }

    public long getLastUpdateTime() {
        return getLong(PREF_LAST_UPDATE_TIME, 0);
    }

    public void setLastUpdateTime(long timestamp) {
        setLong(PREF_LAST_UPDATE_TIME, timestamp);
    }

    public long getLastUpdateCheckTime() {
        return getLong(PREF_LAST_UPDATE_CHECK_TIME, 0);
    }

    public void setLastUpdateCheckTime(long timestamp) {
        setLong(PREF_LAST_UPDATE_CHECK_TIME, timestamp);
    }

}
