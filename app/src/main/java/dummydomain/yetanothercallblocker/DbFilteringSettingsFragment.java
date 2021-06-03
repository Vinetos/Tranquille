package dummydomain.yetanothercallblocker;

import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;

import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.data.YacbHolder;
import dummydomain.yetanothercallblocker.utils.DbFilteringUtils;
import dummydomain.yetanothercallblocker.work.TaskService;

import static dummydomain.yetanothercallblocker.Settings.PREF_DB_FILTERING_PREFIXES_TO_KEEP;

public class DbFilteringSettingsFragment extends BaseSettingsFragment {

    private static final String PREF_SCREEN_DB_FILTERING = "dbFiltering";
    private static final String PREF_INFO = "dbFilteringInfo";
    private static final String PREF_FILTER_DB = "dbFilteringFilterDb";

    private final Settings settings = App.getSettings();

    @Override
    protected String getScreenKey() {
        return PREF_SCREEN_DB_FILTERING;
    }

    @Override
    protected int getPreferencesResId() {
        return R.xml.db_filtering_preferences;
    }

    @Override
    protected void initScreen() {
        requirePreference(PREF_INFO).setOnPreferenceClickListener(pref -> {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.settings_screen_db_filtering)
                    .setMessage(pref.getSummary())
                    .setNegativeButton(R.string.back, null)
                    .show();
            return true;
        });

        setPrefChangeListener(PREF_DB_FILTERING_PREFIXES_TO_KEEP, (pref, newValue) -> {
            String value = (String) newValue;

            List<String> prefixes = new ArrayList<>();
            for (String prefix : DbFilteringUtils.parsePrefixes(value)) {
                prefixes.add("+" + prefix);
            }
            String formattedPrefixes = TextUtils.join(",", prefixes);

            if (!TextUtils.equals(formattedPrefixes, value)) {
                ((EditTextPreference) pref).setText(formattedPrefixes);
                return false;
            }

            return true;
        });

        requirePreference(PREF_FILTER_DB).setOnPreferenceClickListener(preference -> {
            updateFilter();
            TaskService.start(requireContext(), TaskService.TASK_FILTER_DB);
            return true;
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        updateFilter();
    }

    private void updateFilter() {
        YacbHolder.getDbManager().setNumberFilter(DbFilteringUtils.getNumberFilter(settings));
    }

}
