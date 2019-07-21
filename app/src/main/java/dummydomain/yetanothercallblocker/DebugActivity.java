package dummydomain.yetanothercallblocker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        hideSummary();
    }

    public void onQueryDbButtonClick(View view) {
        setResult("");
        hideSummary();

        new AsyncTask<Void, Void, CommunityDatabaseItem>() {
            @Override
            protected CommunityDatabaseItem doInBackground(Void... voids) {
                CommunityDatabaseItem item = DatabaseSingleton.getCommunityDatabase()
                        .getDbItemByNumber(getNumber());

                return item;
            }

            @Override
            protected void onPostExecute(CommunityDatabaseItem item) {
                String string;
                if (item != null) {
                    string = item.getNumber() + "\n";

                    if (item.getUnknownData() != 0) {
                        string += "unknownData=" + item.getUnknownData() + "\n";
                    }

                    NumberCategory category = NumberCategory.getById(item.getCategory());
                    if (category != null) {
                        string += DebugActivity.this.getString(category.getStringId());
                    } else {
                        string += "category=" + item.getCategory() + "\n";
                    }

                    displaySummary(item);
                } else {
                    string = DebugActivity.this.getString(R.string.debug_not_found);
                }
                setResult(string);
            }
        }.execute();
    }

    public void onQueryFeaturedDbButtonClick(View view) {
        setResult("");
        hideSummary();

        new AsyncTask<Void, Void, FeaturedDatabaseItem>() {
            @Override
            protected FeaturedDatabaseItem doInBackground(Void... voids) {
                FeaturedDatabaseItem item = DatabaseSingleton.getFeaturedDatabase()
                        .getDbItemByNumber(getNumber());

                return item;
            }

            @Override
            protected void onPostExecute(FeaturedDatabaseItem item) {
                setResult(item != null ? item.toString()
                        : DebugActivity.this.getString(R.string.debug_not_found));
            }
        }.execute();
    }

    public void onLoadReviewsButtonClick(View view) {
        ReviewsActivity.startForNumber(this, getNumber());
    }

    public void onUpdateDbButtonClick(View view) {
        setResult("");
        hideSummary();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseSingleton.getCommunityDatabase().updateSecondaryDb();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setResult(DebugActivity.this.getString(R.string.debug_update_result,
                        DatabaseSingleton.getCommunityDatabase().getEffectiveDbVersion()));
            }
        }.execute();
    }

    private String getNumber() {
        return this.<EditText>findViewById(R.id.debugPhoneNumberEditText).getText().toString();
    }

    private void setResult(String result) {
        this.<TextView>findViewById(R.id.debugResultsTextView).setText(result);
    }

    private void hideSummary() {
        findViewById(R.id.reviews_summary).setVisibility(View.GONE);
    }

    private void displaySummary(CommunityDatabaseItem item) {
        View summary = DebugActivity.this.findViewById(R.id.reviews_summary);
        summary.setVisibility(View.VISIBLE);

        Map<Integer, Integer> map = new HashMap<>();
        map.put(R.id.summary_text_negative, item.getNegativeRatingsCount());
        map.put(R.id.summary_text_neutral, item.getNeutralRatingsCount());
        map.put(R.id.summary_text_positive, item.getPositiveRatingsCount());
        for (Map.Entry<Integer, Integer> e: map.entrySet()) {
            ((TextView) summary.findViewById(e.getKey())).setText(
                    String.valueOf(e.getValue()));
        }
    }

}
