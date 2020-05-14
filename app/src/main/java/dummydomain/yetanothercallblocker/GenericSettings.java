package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.SharedPreferences;

public class GenericSettings {

    protected final SharedPreferences pref;

    public GenericSettings(Context context, String name) {
        this(context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    public GenericSettings(SharedPreferences pref) {
        this.pref = pref;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    public int getInt(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    public void setInt(String key, int value) {
        pref.edit().putInt(key, value).apply();
    }

    public long getLong(String key, long defValue) {
        return pref.getLong(key, defValue);
    }

    public void setLong(String key, long value) {
        pref.edit().putLong(key, value).apply();
    }

}
