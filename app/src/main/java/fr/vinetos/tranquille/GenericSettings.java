package fr.vinetos.tranquille;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.core.util.Supplier;

import java.util.Set;

public class GenericSettings {

    protected final Context context;
    protected final SharedPreferences pref;

    public GenericSettings(Context context, String name) {
        this(context, context.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    public GenericSettings(Context context, SharedPreferences pref) {
        this.context = context;
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

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    public String getNonEmptyString(String key, String defValue) {
        String value = getString(key);
        if (TextUtils.isEmpty(value)) value = defValue;
        return value;
    }

    public void setString(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public Set<String> getStringSet(String key, Set<String> defValue) {
        return pref.getStringSet(key, defValue);
    }

    public Set<String> getStringSet(String key, Supplier<Set<String>> defValueSupplier) {
        Set<String> val = pref.getStringSet(key, null);
        return val != null ? val : defValueSupplier.get();
    }

    public void setStringSet(String key, Set<String> value) {
        pref.edit().putStringSet(key, value).apply();
    }

    public boolean isSet(String key) {
        return pref.contains(key);
    }

    public void unset(String key) {
        pref.edit().remove(key).apply();
    }

}
