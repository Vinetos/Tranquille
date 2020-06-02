package dummydomain.yetanothercallblocker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import dummydomain.yetanothercallblocker.data.DatabaseSingleton;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;
import dummydomain.yetanothercallblocker.work.TaskService;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        hideSummary();

        onDbInfoButtonClick(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onSecondaryDbUpdateFinished(SecondaryDbUpdateFinished event) {
        setResult(getString(R.string.debug_update_result,
                DatabaseSingleton.getCommunityDatabase().getEffectiveDbVersion()));
    }

    public void onQueryDbButtonClick(View view) {
        setResult("");
        hideSummary();

        new AsyncTask<Void, Void, Pair<CommunityDatabaseItem, FeaturedDatabaseItem>>() {
            @Override
            protected Pair<CommunityDatabaseItem, FeaturedDatabaseItem> doInBackground(Void... voids) {
                CommunityDatabaseItem item = DatabaseSingleton.getCommunityDatabase()
                        .getDbItemByNumber(getNumber());

                FeaturedDatabaseItem featuredItem = DatabaseSingleton.getFeaturedDatabase()
                        .getDbItemByNumber(getNumber());

                return new Pair<>(item, featuredItem);
            }

            @Override
            protected void onPostExecute(Pair<CommunityDatabaseItem, FeaturedDatabaseItem> result) {
                CommunityDatabaseItem item = result.first;
                FeaturedDatabaseItem featuredItem = result.second;

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

                if (featuredItem != null) {
                    string += "\n" + "Featured name: " + featuredItem.getName();
                }

                setResult(string);
            }
        }.execute();
    }

    public void onLoadReviewsButtonClick(View view) {
        ReviewsActivity.startForNumber(this, getNumber());
    }

    public void onDbInfoButtonClick(View view) {
        setResult("");
        hideSummary();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder sb = new StringBuilder();

                CommunityDatabase communityDatabase = DatabaseSingleton.getCommunityDatabase();

                sb.append("DB info:\n");
                sb.append("Operational: ").append(communityDatabase.isOperational()).append('\n');
                sb.append("Base version: ").append(communityDatabase.getBaseDbVersion());
                sb.append(" (SIA: ").append(communityDatabase.getSiaAppVersion()).append(")\n");
                sb.append("Effective version: ").append(communityDatabase.getEffectiveDbVersion()).append('\n');
                sb.append("Last update time: ").append(dateOrNever(App.getSettings().getLastUpdateTime())).append('\n');
                sb.append("Last update check time: ").append(dateOrNever(App.getSettings().getLastUpdateCheckTime())).append('\n');

                FeaturedDatabase featuredDatabase = DatabaseSingleton.getFeaturedDatabase();

                sb.append("Featured DB info:\n");
                sb.append("Operational: ").append(featuredDatabase.isOperational()).append('\n');
                sb.append("Effective version: ").append(featuredDatabase.getBaseDbVersion()).append('\n');

                return sb.toString();
            }

            private String dateOrNever(long time) {
                if (time > 0) return new Date(time).toString();
                return "never";
            }

            @Override
            protected void onPostExecute(String info) {
                setResult(info);
            }
        }.execute();
    }

    public void onUpdateDbButtonClick(View view) {
        setResult("");
        hideSummary();

        TaskService.start(this, TaskService.TASK_UPDATE_SECONDARY_DB);
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
        ReviewsSummaryHelper.populateSummary(findViewById(R.id.reviews_summary), item);
    }

}
