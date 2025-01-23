package fr.vinetos.tranquille.presentation.denylist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import fr.vinetos.tranquille.App
import fr.vinetos.tranquille.CustomVerticalDivider
import fr.vinetos.tranquille.DenylistItemRecyclerViewAdapter
import fr.vinetos.tranquille.EventUtils
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.data.DenylistImporterExporter
import fr.vinetos.tranquille.data.DenylistItem
import fr.vinetos.tranquille.data.YacbHolder
import fr.vinetos.tranquille.DenylistDataSource
import fr.vinetos.tranquille.data.datasource.DenylistDao
import fr.vinetos.tranquille.domain.service.DenylistService
import fr.vinetos.tranquille.event.DenylistChangedEvent
import fr.vinetos.tranquille.utils.FileUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.util.Objects

class DenylistActivity : AppCompatActivity() {

    private val LOG: Logger = LoggerFactory.getLogger(DenylistActivity::class.java)

    private val settings = App.getSettings()

    private lateinit var denylistDao: DenylistDao
    private lateinit var denylistService: DenylistService

    private lateinit var recyclerView: RecyclerView
    private lateinit var denylistAdapter: DenylistItemRecyclerViewAdapter
    private lateinit var denylistDataSourceFactory: DenylistDataSource.Factory

    private var selectionTracker: SelectionTracker<Long>? = null
    private var actionModeCallback: ActionMode.Callback? = null
    private var actionMode: ActionMode? = null

    private var listLayoutManagerSavedState: Parcelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denylist)

        denylistDao = YacbHolder.getDenylistDao()
        denylistService = YacbHolder.getDenylistService()

        denylistAdapter = DenylistItemRecyclerViewAdapter(this::onItemClicked)
        recyclerView = findViewById(R.id.denylistItemsList)
        recyclerView.apply {
            adapter = denylistAdapter
            addItemDecoration(CustomVerticalDivider(this@DenylistActivity))
        }

        selectionTracker = SelectionTracker.Builder(
            "denylistSelection", recyclerView,
            denylistAdapter.itemKeyProvider,
            denylistAdapter.getItemDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).build()
        denylistAdapter.setSelectionTracker(selectionTracker);

        actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.activity_denylist_action_mode, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (item.itemId == R.id.menu_select_all) {
                    selectionTracker!!.setItemsSelected(
                        denylistDataSourceFactory.currentDataSource!!.getAllIds(),
                        true
                    )
                    return true
                } else if (item.itemId == R.id.menu_delete) {
                    AlertDialog.Builder(this@DenylistActivity)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.denylist_delete_confirmation)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            if (selectionTracker!!.hasSelection()) {
                                denylistService.delete(selectionTracker!!.selection)
                                selectionTracker!!.clearSelection()
                            }
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                selectionTracker!!.clearSelection()
                actionMode = null
            }
        }

        selectionTracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                if (selectionTracker!!.hasSelection()) {
                    if (actionMode == null) {
                        actionMode = startSupportActionMode(actionModeCallback!!)
                    }
                } else {
                    if (actionMode != null) {
                        actionMode!!.finish()
                        actionMode = null
                    }
                }

                if (actionMode != null) {
                    val count = selectionTracker!!.selection.size()
                    actionMode!!.title = resources.getQuantityString(
                        R.plurals.selected_count, count, count
                    )
                }
            }
        })

        var initialKey: Int? = null
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_LIST_LAST_KEY)) {
                initialKey = savedInstanceState.getInt(STATE_LIST_LAST_KEY)
            }

            listLayoutManagerSavedState = savedInstanceState
                .getParcelable(STATE_LIST_LAYOUT_MANAGER)
        }

        denylistDataSourceFactory = denylistDao.dataSourceFactory()

        val config: PagedList.Config = PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(60)
            .build()

        val itemLiveData : LiveData<PagedList<DenylistItem>> =
            LivePagedListBuilder(denylistDataSourceFactory, config)
                .setInitialLoadKey(initialKey)
                .build()

        itemLiveData.observe(this) { data: PagedList<DenylistItem>? ->
            denylistAdapter.submitList(data)
            if (listLayoutManagerSavedState != null) {
                Objects.requireNonNull<RecyclerView.LayoutManager?>(recyclerView.layoutManager)
                    .onRestoreInstanceState(listLayoutManagerSavedState)

                listLayoutManagerSavedState = null
            }
        }

        selectionTracker!!.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_denylist, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_block_denylisted)?.setChecked(settings.blockDenylisted)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()

        EventUtils.register(this)

        if (activityFirstStart) {
            activityFirstStart = false
        } else {
            reloadItems()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        selectionTracker!!.onSaveInstanceState(outState)

        val currentList: PagedList<DenylistItem>? = denylistAdapter.currentList
        if (currentList != null) {
            val lastKey = currentList.lastKey as Int?
            if (lastKey != null) {
                outState.putInt(STATE_LIST_LAST_KEY, lastKey)
            }
        }

        outState.putParcelable(
            STATE_LIST_LAYOUT_MANAGER,
            recyclerView.layoutManager!!.onSaveInstanceState()
        )
    }

    override fun onStop() {
        EventUtils.unregister(this)

        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMPORT && resultCode == RESULT_OK && data != null && data.data != null) {
            var error = false

            var pfd: ParcelFileDescriptor? = null
            try {
                try {
                    pfd = contentResolver.openFileDescriptor(data.data!!, "r")
                } catch (e: FileNotFoundException) {
                    error = true
                    LOG.warn("onActivityResult() get file for import result", e)
                }

                if (pfd != null) {
                    if (DenylistImporterExporter().importDenylist(
                            denylistDao,
                            YacbHolder.getDenylistService(),
                            pfd.fileDescriptor
                        )
                    ) {
                        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show()
                    } else {
                        error = true
                    }
                }
            } finally {
                if (pfd != null) {
                    try {
                        pfd.close()
                    } catch (ignored: IOException) {
                    }
                }
            }

            if (error) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDenylistChanged(denylistChangedEvent: DenylistChangedEvent?) {
        reloadItems()
    }

    private fun reloadItems() {
        denylistDataSourceFactory.invalidate()
    }

    fun onBlockDenylistedChanged(item: MenuItem) {
        settings.blockDenylisted = !item.isChecked
    }

    fun onAddClicked(view: View?) {
        startActivity(EditDenylistItemActivity.getIntent(this, null, null))
    }

    private fun onItemClicked(denylistItem: DenylistItem) {
        startActivity(EditDenylistItemActivity.getIntent(this, denylistItem.id))
    }

    fun onExportDenylistClicked(item: MenuItem?) {
        val file = exportDenylist()
        if (file != null) {
            FileUtils.shareFile(this, file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportDenylist(): File? {
        val file = File(cacheDir, "Tranquille_backup.csv")
        try {
            if (!file.exists() && !file.createNewFile()) return null

            FileWriter(file).use { writer ->
                if (DenylistImporterExporter().writeBackup(denylistDao.getAll(), writer)) {
                    return file
                }
            }
        } catch (e: IOException) {
            LOG.warn("exportDenylist()", e)
        }

        return null
    }

    fun onImportDenylistClicked(item: MenuItem?) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")

        try {
            startActivityForResult(intent, REQUEST_CODE_IMPORT)
        } catch (e: ActivityNotFoundException) {
            LOG.warn("onImportDenylistClicked()", e)
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_IMPORT: Int = 1
        private const val STATE_LIST_LAST_KEY: String = "list_last_key"
        private const val STATE_LIST_LAYOUT_MANAGER: String = "list_layout_manager"
        private var activityFirstStart = true
    }
}
