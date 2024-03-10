package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;

import java.util.List;

import fr.vinetos.tranquille.data.YacbHolder;
import fr.vinetos.tranquille.utils.DbFilteringUtils;
import fr.vinetos.tranquille.work.TaskService;

import static fr.vinetos.tranquille.Settings.PREF_DB_FILTERING_PREFIXES_TO_KEEP;

public class DbFilteringSettingsFragment extends BaseSettingsFragment {

    private static final String PREF_SCREEN_DB_FILTERING = "dbFiltering";
    private static final String PREF_INFO = "dbFilteringInfo";
    private static final String PREF_FILTER_DB = "dbFilteringFilterDb";

    private final Settings settings = App.getSettings();

    private AsyncTask<Void, Void, List<String>> prefillPrefixesTask;

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
        if (!settings.isDbFilteringPrefixesPrefilled()) {
            settings.setDbFilteringPrefixesPrefilled(true);

            if (TextUtils.isEmpty(settings.getDbFilteringPrefixesToKeep())) {
                startPrefillPrefixesTask();
            }
        }

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

            String formattedPrefixes = DbFilteringUtils.formatPrefixes(
                    DbFilteringUtils.parsePrefixes(value));

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
        cancelPrefillPrefixesTask();

        updateFilter();

        super.onStop();
    }

    private void startPrefillPrefixesTask() {
        cancelPrefillPrefixesTask();
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<String>> prefillPrefixesTask = this.prefillPrefixesTask
                = new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... voids) {
                return DbFilteringUtils.detectPrefixes(requireContext(),
                        settings.getCachedAutoDetectedCountryCode());
            }

            @Override
            protected void onPostExecute(List<String> prefixList) {
                if (!prefixList.isEmpty()) {
                    EditTextPreference preference = requirePreference(
                            PREF_DB_FILTERING_PREFIXES_TO_KEEP);

                    if (TextUtils.isEmpty(preference.getText())) {
                        preference.setText(DbFilteringUtils.formatPrefixes(prefixList));
                    }
                }
            }
        };
        prefillPrefixesTask.execute();
    }

    private void cancelPrefillPrefixesTask() {
        if (prefillPrefixesTask != null) {
            prefillPrefixesTask.cancel(true);
            prefillPrefixesTask = null;
        }
    }

    private void updateFilter() {
        YacbHolder.getDbManager().setNumberFilter(DbFilteringUtils.getNumberFilter(settings));
    }

}
