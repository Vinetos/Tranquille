package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import dummydomain.yetanothercallblocker.data.CountryHelper;

public class Settings extends GenericSettings {

    public static final String PREF_INCOMING_CALL_NOTIFICATIONS = "incomingCallNotifications";
    public static final String PREF_BLOCK_CALLS = "blockCalls";
    public static final String PREF_USE_CONTACTS = "useContacts";
    public static final String PREF_NUMBER_OF_RECENT_CALLS = "numberOfRecentCalls";
    public static final String PREF_NOTIFICATIONS_KNOWN = "showNotificationsForKnownCallers";
    public static final String PREF_NOTIFICATIONS_UNKNOWN = "showNotificationsForUnknownCallers";
    public static final String PREF_LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String PREF_LAST_UPDATE_CHECK_TIME = "lastUpdateCheckTime";
    public static final String PREF_COUNTRY_CODE_OVERRIDE = "countryCodeOverride";
    public static final String PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE = "countryCodeForReviewsOverride";

    static final String SYS_PREFERENCES_VERSION = "__preferencesVersion";

    private static final int PREFERENCES_VERSION = 1;

    private volatile String cachedAutoDetectedCountryCode;

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

    public int getNumberOfRecentCalls() {
        return getInt(PREF_NUMBER_OF_RECENT_CALLS, 20);
    }

    public void setNumberOfRecentCalls(int number) {
        setInt(PREF_NUMBER_OF_RECENT_CALLS, number);
    }

    public boolean getNotificationsForKnownCallers() {
        return getBoolean(PREF_NOTIFICATIONS_KNOWN);
    }

    public void setNotificationsForKnownCallers(boolean show) {
        setBoolean(PREF_NOTIFICATIONS_KNOWN, show);
    }

    public boolean getNotificationsForUnknownCallers() {
        return getBoolean(PREF_NOTIFICATIONS_UNKNOWN);
    }

    public void setNotificationsForUnknownCallers(boolean show) {
        setBoolean(PREF_NOTIFICATIONS_UNKNOWN, show);
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

    public String getCountryCodeOverride() {
        return getString(PREF_COUNTRY_CODE_OVERRIDE);
    }

    public void setCountryCodeOverride(String code) {
        setString(PREF_COUNTRY_CODE_OVERRIDE, code);
    }

    public String getCountryCodeForReviewsOverride() {
        return getString(PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE);
    }

    public void setCountryCodeForReviewsOverride(String code) {
        setString(PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE, code);
    }

    public String getCountryCode() {
        String override = getCountryCodeOverride();
        if (!TextUtils.isEmpty(override)) return override.toUpperCase();

        return getCachedAutoDetectedCountryCode();
    }

    public String getCountryCodeForReviews() {
        String override = getCountryCodeForReviewsOverride();
        if (!TextUtils.isEmpty(override)) return override.toUpperCase();

        String code = getCachedAutoDetectedCountryCode();
        return !TextUtils.isEmpty(code) ? code : "US";
    }

    public String getCachedAutoDetectedCountryCode() {
        String code = cachedAutoDetectedCountryCode;
        if (code == null) {
            code = CountryHelper.detectCountry(context);
            if (TextUtils.isEmpty(code)) code = "";

            cachedAutoDetectedCountryCode = code;
        }
        return code;
    }

}
