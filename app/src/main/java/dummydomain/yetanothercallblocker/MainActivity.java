package dummydomain.yetanothercallblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;

public class MainActivity extends AppCompatActivity {

    private CallLogItemRecyclerViewAdapter callLogAdapter;
    private List<CallLogItem> callLogItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callLogAdapter = new CallLogItemRecyclerViewAdapter(callLogItems, this::onCallLogItemClicked);
        RecyclerView recyclerView = findViewById(R.id.callLogList);
        recyclerView.setAdapter(callLogAdapter);

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

        loadCallLog();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadCallLog();
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

    private void onCallLogItemClicked(CallLogItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(item.number);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.info_dialog, null);

        TextView featuredNameView = view.findViewById(R.id.featuredName);
        if (item.numberInfo.name != null) {
            featuredNameView.setText(item.numberInfo.name);
        } else {
            featuredNameView.setVisibility(View.GONE);
        }

        ReviewsSummaryHelper.populateSummary(view.findViewById(R.id.reviews_summary),
                item.numberInfo.communityDatabaseItem);

        builder.setView(view);

        builder.setNeutralButton(R.string.online_reviews, (d, w)
                -> ReviewsActivity.startForNumber(this, item.number));

        builder.setNegativeButton(R.string.back, null);

        builder.show();
    }

    enum UpdateUiState {HIDDEN, NO_DB, DOWNLOADING_DB, ERROR}

    private void updateNoDbUi(UpdateUiState state) {
        findViewById(R.id.noDbText).setVisibility(state == UpdateUiState.NO_DB ? View.VISIBLE : View.GONE);
        findViewById(R.id.downloadDbButton).setVisibility(state == UpdateUiState.NO_DB ? View.VISIBLE : View.GONE);

        findViewById(R.id.downloadingDbText).setVisibility(state == UpdateUiState.DOWNLOADING_DB ? View.VISIBLE : View.GONE);

        findViewById(R.id.dbCouldNotBeDownloaded).setVisibility(state == UpdateUiState.ERROR ? View.VISIBLE : View.GONE);
    }

    private void loadCallLog() {
        if (!PermissionHelper.havePermission(this, Manifest.permission.READ_CALL_LOG)) {
            setCallLogVisibility(false);
            return;
        }

        new AsyncTask<Void, Void, List<CallLogItem>>() {
            @Override
            protected List<CallLogItem> doInBackground(Void... voids) {
                List<CallLogItem> items = CallLogHelper.getRecentCalls(MainActivity.this, 10);

                for (CallLogItem item : items) {
                    item.numberInfo = DatabaseSingleton.getCommunityDatabase().isOperational()
                            ? DatabaseSingleton.getNumberInfo(item.number)
                            : new NumberInfo();
                }

                return items;
            }

            @Override
            protected void onPostExecute(List<CallLogItem> items) {
                callLogItems.clear();
                callLogItems.addAll(items);
                callLogAdapter.notifyDataSetChanged();

                setCallLogVisibility(true);
            }
        }.execute();
    }

    private void setCallLogVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.callLogTitle).setVisibility(visibility);
        findViewById(R.id.callLogList).setVisibility(visibility);
    }

}
