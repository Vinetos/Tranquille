package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import fr.vinetos.tranquille.data.YacbHolder;
import fr.vinetos.tranquille.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import fr.vinetos.tranquille.work.TaskService;

public class DbManagementActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, String> dbInfoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_management);

        onDbInfoButtonClick(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventUtils.register(this);
    }

    @Override
    protected void onStop() {
        EventUtils.unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cancelDbInfoTask();

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onSecondaryDbUpdateFinished(SecondaryDbUpdateFinished event) {
        setResult(getString(R.string.db_management_update_result,
                YacbHolder.getCommunityDatabase().getEffectiveDbVersion()));
    }

    public void onResetDbClick(View view) {
        clearMessage();

        YacbHolder.getCommunityDatabase().resetSecondaryDatabase();

        YacbHolder.getDbManager().removeMainDb();
        YacbHolder.getCommunityDatabase().reload();
        YacbHolder.getFeaturedDatabase().reload();
        YacbHolder.getSiaMetadata().reload();

        setResult("Database removed");
    }

    public void onResetSecondaryDbClick(View view) {
        clearMessage();

        YacbHolder.getCommunityDatabase().resetSecondaryDatabase();

        setResult("Secondary database removed");
    }

    public void onDbInfoButtonClick(View view) {
        clearMessage();

        startDbInfoTask();
    }

    public void onUpdateDbButtonClick(View view) {
        clearMessage();

        TaskService.start(this, TaskService.TASK_UPDATE_SECONDARY_DB);
    }

    private void startDbInfoTask() {
        cancelDbInfoTask();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> task = this.dbInfoTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder sb = new StringBuilder();

                SiaMetadata siaMetadata = YacbHolder.getSiaMetadata();
                CommunityDatabase communityDatabase = YacbHolder.getCommunityDatabase();

                sb.append("DB info:\n");
                sb.append("Operational: ").append(communityDatabase.isOperational()).append('\n');
                sb.append("Base version: ").append(communityDatabase.getBaseDbVersion());
                sb.append(" (SIA: ").append(siaMetadata.getSiaAppVersion()).append(")\n");
                sb.append("Effective version: ").append(communityDatabase.getEffectiveDbVersion()).append('\n');
                sb.append("Last update time: ").append(dateOrNever(App.getSettings().getLastUpdateTime())).append('\n');
                sb.append("Last update check time: ").append(dateOrNever(App.getSettings().getLastUpdateCheckTime())).append('\n');

                FeaturedDatabase featuredDatabase = YacbHolder.getFeaturedDatabase();

                sb.append("\nFeatured DB info:\n");
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
        };
        task.execute();
    }

    private void cancelDbInfoTask() {
        if (dbInfoTask != null) {
            dbInfoTask.cancel(true);
            dbInfoTask = null;
        }
    }

    private void clearMessage() {
        setResult("");
    }

    private void setResult(String result) {
        this.<TextView>findViewById(R.id.debugResultsTextView).setText(result);
    }

}
