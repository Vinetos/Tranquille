package dummydomain.yetanothercallblocker.sia;

import android.content.Context;
import android.content.SharedPreferences;

public class SiaSettings {

    private static final String PREF_BASE_DB_VERSION = "baseDbVersion";
    private static final String PREF_SECONDARY_DB_VERSION = "secondaryDbVersion";

    private final SharedPreferences pref;

    public SiaSettings(Context context) {
        pref = context.getSharedPreferences("sia_preferences", Context.MODE_PRIVATE);
    }

    public int getBaseDbVersion() {
        return pref.getInt(PREF_BASE_DB_VERSION, 0);
    }

    public void setBaseDbVersion(int version) {
        pref.edit().putInt(PREF_BASE_DB_VERSION, version).apply();
    }

    public int getSecondaryDbVersion() {
        return pref.getInt(PREF_SECONDARY_DB_VERSION, 0);
    }

    public void setSecondaryDbVersion(int version) {
        pref.edit().putInt(PREF_SECONDARY_DB_VERSION, version).apply();
    }

}
