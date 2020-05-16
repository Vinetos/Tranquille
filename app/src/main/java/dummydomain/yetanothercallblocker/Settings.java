package dummydomain.yetanothercallblocker;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class Settings extends GenericSettings {

    public static final String PREF_INCOMING_CALL_NOTIFICATIONS = "incomingCallNotifications";
    public static final String PREF_BLOCK_CALLS = "blockCalls";
    public static final String PREF_USE_CONTACTS = "useContacts";
    public static final String PREF_LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String PREF_LAST_UPDATE_CHECK_TIME = "lastUpdateCheckTime";

    private static final String SYS_PREFERENCES_VERSION = "__preferencesVersion";

    private static final int PREFERENCES_VERSION = 1;

    Settings(Context context) {
        super(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    private Settings(Context context, String name) {
        super(context, name);
    }

    public void init() {
        int preferencesVersion = getInt(SYS_PREFERENCES_VERSION, -1);

        if (preferencesVersion == PREFERENCES_VERSION) return;

        if (preferencesVersion < 1) {
            PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false);

            Settings oldSettings = new Settings(context, "yacb_preferences");

            if (oldSettings.isSet(PREF_INCOMING_CALL_NOTIFICATIONS)) {
                setIncomingCallNotifications(oldSettings.getIncomingCallNotifications());
            }
            if (oldSettings.isSet(PREF_BLOCK_CALLS)) {
                setBlockCalls(oldSettings.getBlockCalls());
            }
            if (oldSettings.isSet(PREF_USE_CONTACTS)) {
                setUseContacts(oldSettings.getUseContacts());
            }
            setLastUpdateTime(oldSettings.getLastUpdateTime());
            setLastUpdateCheckTime(oldSettings.getLastUpdateCheckTime());
        }

        setInt(SYS_PREFERENCES_VERSION, PREFERENCES_VERSION);
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
