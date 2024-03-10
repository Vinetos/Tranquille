package fr.vinetos.tranquille.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class IntEditTextPreference extends EditTextPreference {

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setListener();
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setListener();
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListener();
    }

    public IntEditTextPreference(Context context) {
        super(context);
        setListener();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int defaultInt = defaultValue != null ? (int) defaultValue : 0;
        setText(String.valueOf(getPersistedInt(defaultInt)));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(!TextUtils.isEmpty(value) ? Integer.parseInt(value) : 0);
    }

    private void setListener() {
        setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
    }

}
