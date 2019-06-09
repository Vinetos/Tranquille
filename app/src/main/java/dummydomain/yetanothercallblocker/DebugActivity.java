package dummydomain.yetanothercallblocker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    public void onQueryDbButtonClick(View view) {
        setResult("");

        new AsyncTask<Void, Void, CommunityDatabaseItem>() {
            @Override
            protected CommunityDatabaseItem doInBackground(Void... voids) {
                CommunityDatabaseItem item = DatabaseSingleton.getCommunityDatabase()
                        .getDbItemByNumber(getNumber());

                return item;
            }

            @Override
            protected void onPostExecute(CommunityDatabaseItem item) {
                setResult(item != null ? item.toString()
                        : DebugActivity.this.getString(R.string.debug_not_found));
            }
        }.execute();
    }

    public void onQueryFeaturedDbButtonClick(View view) {
        setResult("");

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

}
