package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwitchCompat notificationsSwitch = findViewById(R.id.notificationsEnabledSwitch);
        notificationsSwitch.setChecked(CallReceiver.isEnabled(this));
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked)
                -> CallReceiver.setEnabled(MainActivity.this, isChecked));

        SwitchCompat blockCallsSwitch = findViewById(R.id.blockCallsSwitch);
        blockCallsSwitch.setChecked(new Settings(this).getBlockCalls());
        blockCallsSwitch.setOnCheckedChangeListener((buttonView, isChecked)
                -> new Settings(this).setBlockCalls(isChecked));

        SwitchCompat autoUpdateSwitch = findViewById(R.id.autoUpdateEnabledSwitch);
        autoUpdateSwitch.setChecked(Updater.isAutoUpdateScheduled());
        autoUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) Updater.scheduleAutoUpdateWorker();
            else Updater.cancelAutoUpdateWorker();
        });

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Boolean> noDbTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return DatabaseSingleton.getCommunityDatabase().isOperational();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                updateNoDbUi(result ? UpdateUiState.HIDDEN : UpdateUiState.NO_DB);
            }
        };
        noDbTask.execute();

        PermissionHelper.checkPermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // TODO: handle
    }

    public void onDownloadDbClick(View view) {
        // TODO: use service

        updateNoDbUi(UpdateUiState.DOWNLOADING_DB);

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Boolean> dlTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return DbManager.downloadMainDb()
                        && DatabaseSingleton.getCommunityDatabase().reload()
                        && DatabaseSingleton.getFeaturedDatabase().reload();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                updateNoDbUi(result ? UpdateUiState.HIDDEN : UpdateUiState.ERROR);
            }
        };
        dlTask.execute();
    }

    public void onOpenDebugActivity(MenuItem item) {
        startActivity(new Intent(this, DebugActivity.class));
    }

    enum UpdateUiState {HIDDEN, NO_DB, DOWNLOADING_DB, ERROR}

    private void updateNoDbUi(UpdateUiState state) {
        findViewById(R.id.noDbText).setVisibility(state == UpdateUiState.NO_DB ? View.VISIBLE : View.GONE);
        findViewById(R.id.downloadDbButton).setVisibility(state == UpdateUiState.NO_DB ? View.VISIBLE : View.GONE);

        findViewById(R.id.downloadingDbText).setVisibility(state == UpdateUiState.DOWNLOADING_DB ? View.VISIBLE : View.GONE);

        findViewById(R.id.dbCouldNotBeDownloaded).setVisibility(state == UpdateUiState.ERROR ? View.VISIBLE : View.GONE);
    }

}
