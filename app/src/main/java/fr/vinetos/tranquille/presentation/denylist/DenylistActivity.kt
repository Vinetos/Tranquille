package fr.vinetos.tranquille.presentation.denylist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.vinetos.tranquille.App
import fr.vinetos.tranquille.CustomVerticalDivider
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.data.YacbHolder
import fr.vinetos.tranquille.data.datasource.DenylistDataSource

class DenylistActivity : AppCompatActivity() {

    private lateinit var denylistItemAdapter: DenylistItemAdapter
    private lateinit var denylistItemListView: RecyclerView
    private lateinit var denylistDataSource: DenylistDataSource

    private val settings = App.getSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blacklist)

        denylistDataSource = YacbHolder.getDenylistDataSource()
        denylistItemAdapter = DenylistItemAdapter(denylistDataSource.getAll())

        denylistItemListView = findViewById(R.id.blacklistItemsList)
        denylistItemListView.apply {
            adapter = denylistItemAdapter
            layoutManager = LinearLayoutManager(this@DenylistActivity)
            addItemDecoration(CustomVerticalDivider(this@DenylistActivity))
        }

        findViewById<FloatingActionButton>(R.id.fab).apply {
            visibility = FloatingActionButton.VISIBLE
            setOnClickListener {
                startActivity(Intent(this@DenylistActivity, EditDenylistItemActivity::class.java))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // TODO: The menu is disabled until import / export is implemented
        // menuInflater.inflate(R.menu.activity_blacklist, menu)
        // return true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // TODO: The menu is disabled until import / export is implemented
        // menu?.findItem(R.id.menu_block_blacklisted)?.setChecked(
        //   settings.blockBlacklisted
        // )
        return super.onPrepareOptionsMenu(menu)
    }

}