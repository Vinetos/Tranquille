package fr.vinetos.tranquille;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.MultiSelectListPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UiUtils {

    @ColorInt
    public static int getColorInt(@NonNull Context context, @ColorRes int colorResId) {
        return ResourcesCompat.getColor(context.getResources(), colorResId, context.getTheme());
    }

    public static String getSummary(@NonNull Context context,
                                    @NonNull MultiSelectListPreference preference) {
        List<String> selectedEntries = getSelectedEntries(preference);

        String valuesString = selectedEntries.isEmpty()
                ? context.getString(R.string.selected_value_nothing)
                : TextUtils.join(", ", selectedEntries);

        return context.getResources().getQuantityString(R.plurals.selected_values,
                selectedEntries.size(), valuesString);
    }

    public static List<String> getSelectedEntries(MultiSelectListPreference preference) {
        CharSequence[] entries = preference.getEntries();
        CharSequence[] entryValues = preference.getEntryValues();
        Set<String> values = preference.getValues();

        if (values.isEmpty()) return Collections.emptyList();

        List<String> result = new ArrayList<>(values.size());

        for (int i = 0; i < entries.length; i++) {
            if (values.contains(entryValues[i].toString())) {
                result.add(entries[i].toString());
            }
        }

        return result;
    }

}
