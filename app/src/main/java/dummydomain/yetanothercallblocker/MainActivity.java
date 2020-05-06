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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.event.MainDbDownloadFinishedEvent;
import dummydomain.yetanothercallblocker.event.MainDbDownloadingEvent;
import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.work.TaskService;

public class MainActivity extends AppCompatActivity {

    private CallLogItemRecyclerViewAdapter callLogAdapter;
    private List<CallLogItem> callLogItems = new ArrayList<>();

    private AsyncTask<Void, Void, Boolean> checkMainDbTask;
    private AsyncTask<Void, Void, List<CallLogItem>> loadCallLogTask;

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

        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        loadCallLog();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        startCheckMainDbTask();

        PermissionHelper.checkPermissions(this);

        loadCallLog();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cancelCheckMainDbTask();
        cancelLoadingCallLogTask();

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMainDbDownloadFinished(MainDbDownloadFinishedEvent event) {
        loadCallLog();
    }

    private void startCheckMainDbTask() {
        cancelCheckMainDbTask();
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, Boolean> checkMainDbTask = this.checkMainDbTask
                = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return DatabaseSingleton.getCommunityDatabase().isOperational();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result && EventBus.getDefault().getStickyEvent(MainDbDownloadingEvent.class) == null) {
                    showNoMainDbDialog();
                }
            }
        };
        checkMainDbTask.execute();
    }

    private void cancelCheckMainDbTask() {
        if (checkMainDbTask != null) {
            checkMainDbTask.cancel(true);
            checkMainDbTask = null;
        }
    }

    private void showNoMainDbDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_main_db_title)
                .setMessage(R.string.no_main_db_text)
                .setPositiveButton(R.string.download_main_db,
                        (d, w) -> downloadMainDb())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public void downloadMainDb() {
        TaskService.start(this, TaskService.TASK_DOWNLOAD_MAIN_DB);
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

    private void loadCallLog() {
        if (!PermissionHelper.havePermission(this, Manifest.permission.READ_CALL_LOG)) {
            setCallLogVisibility(false);
            return;
        }

        cancelLoadingCallLogTask();
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<CallLogItem>> loadCallLogTask = this.loadCallLogTask
                = new AsyncTask<Void, Void, List<CallLogItem>>() {
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
        };
        loadCallLogTask.execute();
    }

    private void cancelLoadingCallLogTask() {
        if (loadCallLogTask != null) {
            loadCallLogTask.cancel(true);
            loadCallLogTask = null;
        }
    }

    private void setCallLogVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.callLogTitle).setVisibility(visibility);
        findViewById(R.id.callLogList).setVisibility(visibility);
    }

}
