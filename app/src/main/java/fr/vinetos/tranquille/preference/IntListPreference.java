package fr.vinetos.tranquille.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

public class IntListPreference extends ListPreference {

    public IntListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IntListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntListPreference(Context context) {
        super(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int defaultInt = defaultValue != null ? (int) defaultValue : 0;
        setValue(String.valueOf(getPersistedInt(defaultInt)));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(!TextUtils.isEmpty(value) ? Integer.parseInt(value) : 0);
    }

}
