package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import dummydomain.yetanothercallblocker.data.CallLogHelper;
import dummydomain.yetanothercallblocker.data.CallLogItem;
import dummydomain.yetanothercallblocker.data.DatabaseSingleton;
import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.event.CallEndedEvent;
import dummydomain.yetanothercallblocker.event.MainDbDownloadFinishedEvent;
import dummydomain.yetanothercallblocker.event.MainDbDownloadingEvent;
import dummydomain.yetanothercallblocker.work.TaskService;
import dummydomain.yetanothercallblocker.work.UpdateScheduler;

public class MainActivity extends AppCompatActivity {

    private final Settings settings = App.getSettings();

    private final UpdateScheduler updateScheduler = UpdateScheduler.get(App.getInstance());

    private CallLogItemRecyclerViewAdapter callLogAdapter;
    private RecyclerView recyclerView;

    private AsyncTask<Void, Void, Boolean> checkMainDbTask;
    private AsyncTask<Void, Void, List<CallLogItem>> loadCallLogTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callLogAdapter = new CallLogItemRecyclerViewAdapter(this::onCallLogItemClicked);
        recyclerView = findViewById(R.id.callLogList);
        recyclerView.setAdapter(callLogAdapter);
        recyclerView.addItemDecoration(new CustomVerticalDivider(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_show_notifications).setChecked(
                settings.getIncomingCallNotifications());

        menu.findItem(R.id.menu_block_calls).setChecked(
                settings.getBlockCalls());

        menu.findItem(R.id.menu_auto_updates).setChecked(
                updateScheduler.isAutoUpdateScheduled());

        menu.findItem(R.id.menu_use_contacts).setChecked(
                settings.getUseContacts());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionHelper.handlePermissionsResult(this, requestCode, permissions, grantResults,
                settings.getIncomingCallNotifications(), settings.getBlockCalls(),
                settings.getUseContacts());

        loadCallLog();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        startCheckMainDbTask();

        checkPermissions();

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
    public void onCallEvent(CallEndedEvent event) {
        new Handler(getMainLooper()).postDelayed(this::loadCallLog, 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMainDbDownloadFinished(MainDbDownloadFinishedEvent event) {
        loadCallLog();
    }

    private void checkPermissions() {
        PermissionHelper.checkPermissions(this,
                settings.getIncomingCallNotifications(), settings.getBlockCalls(),
                settings.getUseContacts());
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

    public void onShowNotificationsChanged(MenuItem item) {
        settings.setIncomingCallNotifications(!item.isChecked());
        checkPermissions();
    }

    public void onBlockCallsChanged(MenuItem item) {
        settings.setBlockCalls(!item.isChecked());
        checkPermissions();
    }

    public void onAutoUpdatesChanged(MenuItem item) {
        if (!item.isChecked()) updateScheduler.scheduleAutoUpdates();
        else updateScheduler.cancelAutoUpdateWorker();
    }

    public void onUseContactsChanged(MenuItem item) {
        settings.setUseContacts(!item.isChecked());
        checkPermissions();
        loadCallLog();
    }

    public void onOpenSettings(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onOpenDebugActivity(MenuItem item) {
        startActivity(new Intent(this, DebugActivity.class));
    }

    private void onCallLogItemClicked(CallLogItem item) {
        InfoDialogHelper.showDialog(this, item.numberInfo, null);
    }

    private void loadCallLog() {
        if (!PermissionHelper.hasCallLogPermission(this)) {
            setCallLogVisibility(false);
            return;
        }

        cancelLoadingCallLogTask();
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<CallLogItem>> loadCallLogTask = this.loadCallLogTask
                = new AsyncTask<Void, Void, List<CallLogItem>>() {
            @Override
            protected List<CallLogItem> doInBackground(Void... voids) {
                List<CallLogItem> items = CallLogHelper.getRecentCalls(MainActivity.this, 20);

                for (CallLogItem item : items) {
                    if (DatabaseSingleton.getCommunityDatabase().isOperational()) {
                        item.numberInfo = DatabaseSingleton.getNumberInfo(item.number);
                    } else {
                        item.numberInfo = new NumberInfo();
                        item.numberInfo.number = item.number;
                    }
                }

                return items;
            }

            @Override
            protected void onPostExecute(List<CallLogItem> items) {
                // workaround for auto-scrolling to first item
                // https://stackoverflow.com/a/44053550
                @SuppressWarnings("ConstantConditions")
                Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
                callLogAdapter.setItems(items);
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

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
        findViewById(R.id.callLogPermissionMessage)
                .setVisibility(visible ? View.GONE : View.VISIBLE);

        int visibility = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.callLogTitle).setVisibility(visibility);
        findViewById(R.id.callLogList).setVisibility(visibility);
    }

}
