package fr.vinetos.tranquille;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import fr.vinetos.tranquille.data.BlacklistService;
import fr.vinetos.tranquille.data.DenylistItem;
import fr.vinetos.tranquille.data.YacbHolder;
import fr.vinetos.tranquille.data.db.DenylistDataSource;
import fr.vinetos.tranquille.event.BlacklistChangedEvent;
import fr.vinetos.tranquille.utils.FileUtils;

public class BlacklistActivity extends AppCompatActivity {

//    private static final int REQUEST_CODE_IMPORT = 1;
//
//    private static final String STATE_LIST_LAST_KEY = "list_last_key";
//    private static final String STATE_LIST_LAYOUT_MANAGER = "list_layout_manager";
//
//    private static final Logger LOG = LoggerFactory.getLogger(BlacklistActivity.class);
//
//    private final Settings settings = App.getSettings();
//    private final DenylistDataSource denylistDataSource = YacbHolder.getBlacklistDao();
//    private final BlacklistService blacklistService = YacbHolder.getBlacklistService();
//
//    private RecyclerView recyclerView;
//    private BlacklistItemRecyclerViewAdapter blacklistAdapter;
//    private BlacklistDataSource.Factory blacklistDataSourceFactory;
//
//    private SelectionTracker<Long> selectionTracker;
//    private ActionMode.Callback actionModeCallback;
//    private ActionMode actionMode;
//
//    private Parcelable listLayoutManagerSavedState;
//
//    private boolean activityFirstStart = true;
//
//    public static Intent getIntent(Context context) {
//        return new Intent(context, BlacklistActivity.class);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_blacklist);
//
//        blacklistAdapter = new BlacklistItemRecyclerViewAdapter(this::onItemClicked);
//        recyclerView = findViewById(R.id.blacklistItemsList);
//        recyclerView.setAdapter(blacklistAdapter);
//        recyclerView.addItemDecoration(new CustomVerticalDivider(this));
//
//        selectionTracker = new SelectionTracker.Builder<>(
//                "blacklistSelection", recyclerView,
//                blacklistAdapter.getItemKeyProvider(),
//                blacklistAdapter.getItemDetailsLookup(recyclerView),
//                StorageStrategy.createLongStorage())
//                .build();
//
//        blacklistAdapter.setSelectionTracker(selectionTracker);
//
//        actionModeCallback = new ActionMode.Callback() {
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                mode.getMenuInflater().inflate(R.menu.activity_blacklist_action_mode, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                if (item.getItemId() == R.id.menu_select_all) {
//                    selectionTracker.setItemsSelected(blacklistDataSourceFactory
//                            .getCurrentDataSource().getAllIds(), true);
//                    return true;
//                } else if (item.getItemId() == R.id.menu_delete) {
//                    new AlertDialog.Builder(BlacklistActivity.this)
//                            .setTitle(R.string.are_you_sure)
//                            .setMessage(R.string.blacklist_delete_confirmation)
//                            .setPositiveButton(R.string.yes, (dialog, which) -> {
//                                if (selectionTracker.hasSelection()) {
//                                    blacklistService.delete(selectionTracker.getSelection());
//                                    selectionTracker.clearSelection();
//                                }
//                            })
//                            .setNegativeButton(R.string.no, null)
//                            .show();
//                    return true;
//                }
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//                selectionTracker.clearSelection();
//                actionMode = null;
//            }
//        };
//
//        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
//            @Override
//            public void onItemStateChanged(@NonNull Long key, boolean selected) {
//                if (selectionTracker.hasSelection()) {
//                    if (actionMode == null) {
//                        actionMode = startSupportActionMode(actionModeCallback);
//                    }
//                } else {
//                    if (actionMode != null) {
//                        actionMode.finish();
//                        actionMode = null;
//                    }
//                }
//
//                if (actionMode != null) {
//                    int count = selectionTracker.getSelection().size();
//                    actionMode.setTitle(getResources().getQuantityString(
//                            R.plurals.selected_count, count, count));
//                }
//            }
//        });
//
//        Integer initialKey = null;
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(STATE_LIST_LAST_KEY)) {
//                initialKey = savedInstanceState.getInt(STATE_LIST_LAST_KEY);
//            }
//
//            listLayoutManagerSavedState = savedInstanceState
//                    .getParcelable(STATE_LIST_LAYOUT_MANAGER);
//        }
//
//        blacklistDataSourceFactory = denylistDataSource.dataSourceFactory();
//
//        PagedList.Config config = new PagedList.Config.Builder()
//                .setPageSize(30)
//                .setInitialLoadSizeHint(60)
//                .build();
//
//        LiveData<PagedList<BlacklistItem>> itemLiveData
//                = new LivePagedListBuilder<>(blacklistDataSourceFactory, config)
//                .setInitialLoadKey(initialKey)
//                .build();
//
//        itemLiveData.observe(this, data -> {
//            blacklistAdapter.submitList(data);
//
//            if (listLayoutManagerSavedState != null) {
//                Objects.requireNonNull(recyclerView.getLayoutManager())
//                        .onRestoreInstanceState(listLayoutManagerSavedState);
//
//                listLayoutManagerSavedState = null;
//            }
//        });
//
//        selectionTracker.onRestoreInstanceState(savedInstanceState);
//    }
//
//
//
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        selectionTracker.onSaveInstanceState(outState);
//
//        PagedList<BlacklistItem> currentList = blacklistAdapter.getCurrentList();
//        if (currentList != null) {
//            Integer lastKey = (Integer) currentList.getLastKey();
//            if (lastKey != null) {
//                outState.putInt(STATE_LIST_LAST_KEY, lastKey);
//            }
//        }
//
//        outState.putParcelable(STATE_LIST_LAYOUT_MANAGER,
//                Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState());
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_CODE_IMPORT && resultCode == Activity.RESULT_OK
//                && data != null && data.getData() != null) {
//            boolean error = false;
//
//            ParcelFileDescriptor pfd = null;
//            try {
//                try {
//                    pfd = getContentResolver().openFileDescriptor(data.getData(), "r");
//                } catch (FileNotFoundException e) {
//                    error = true;
//                    LOG.warn("onActivityResult() get file for import result", e);
//                }
//
//                if (pfd != null) {
////                    if (new BlacklistImporterExporter().importBlacklist(
////                            YacbHolder.getBlacklistDao(), YacbHolder.getBlacklistService(),
////                            pfd.getFileDescriptor())) {
////                        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
////                    } else {
////                        error = true;
////                    }
//                }
//            } finally {
//                if (pfd != null) {
//                    try {
//                        pfd.close();
//                    } catch (IOException ignored) {
//                    }
//                }
//            }
//
//            if (error) {
//                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
//    public void onBlacklistChanged(BlacklistChangedEvent blacklistChangedEvent) {
//        reloadItems();
//    }
//
//    private void reloadItems() {
//        blacklistDataSourceFactory.invalidate();
//    }
//
//    public void onBlockBlacklistedChanged(MenuItem item) {
//        settings.setBlockBlacklisted(!item.isChecked());
//    }
//
//    public void onAddClicked(View view) {
//        startActivity(EditBlacklistItemActivity.getIntent(this, null, null));
//    }
//
//    private void onItemClicked(DenylistItem blacklistItem) {
//        startActivity(EditBlacklistItemActivity.getIntent(this, blacklistItem.getId()));
//    }
//
//    public void onExportBlacklistClicked(MenuItem item) {
//        File file = exportBlacklist();
//        if (file != null) {
//            FileUtils.shareFile(this, file);
//        } else {
//            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private File exportBlacklist() {
//        throw new UnsupportedOperationException("Method not implemented");
////        File file = new File(getCacheDir(), "YetAnotherCallBlocker_backup.csv");
////        try {
////            if (!file.exists() && !file.createNewFile()) return null;
////
////            try (FileWriter writer = new FileWriter(file)) {
////                if (new BlacklistImporterExporter().writeBackup(denylistDataSource.loadAll(), writer)) {
////                    return file;
////                }
////            }
////        } catch (IOException e) {
////            LOG.warn("exportBlacklist()", e);
////        }
////
////        return null;
//    }
//
//    public void onImportBlacklistClicked(MenuItem item) {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//k
//        try {
//            startActivityForResult(intent, REQUEST_CODE_IMPORT);
//        } catch (ActivityNotFoundException e) {
//            LOG.warn("onImportBlacklistClicked()", e);
//            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
//        }
//    }

}
