package fr.vinetos.tranquille.presentation.denylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import fr.vinetos.tranquille.App
import fr.vinetos.tranquille.PermissionHelper
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.data.DenylistItem
import fr.vinetos.tranquille.data.DenylistUtils
import fr.vinetos.tranquille.data.YacbHolder
import fr.vinetos.tranquille.data.datasource.DenylistDao
import fr.vinetos.tranquille.domain.service.DenylistService
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Date

class EditDenylistItemActivity : AppCompatActivity() {
    private val denylistDao: DenylistDao = YacbHolder.getDenylistDao()
    private val denylistService: DenylistService = YacbHolder.getDenylistService()

    private lateinit var nameTextField: TextInputLayout
    private lateinit var patternTextField: TextInputLayout

    private var denylistItem: DenylistItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_denylist_item)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        nameTextField = findViewById(R.id.nameTextField)
        patternTextField = findViewById(R.id.patternTextField)

        val patternEditText = patternTextField.editText!!
        patternEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                validate()
            }
        })
        patternEditText.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSaveClicked(null)
                return@setOnEditorActionListener true
            }
            false
        }

        val itemIdFromParams = intent.getLongExtra(PARAM_ITEM_ID, -1)
        if (itemIdFromParams != -1L) {
            denylistItem = runBlocking {
                denylistDao.findById(itemIdFromParams)
            }
            if (denylistItem == null) {
                LOG.warn("onCreate() no item with id={}", itemIdFromParams)
                finish()
                return
            }

            setTitle(R.string.title_edit_denylist_item_activity)
        }

        if (savedInstanceState == null) {
            val name: String?
            var pattern: String?

            if (denylistItem != null) {
                name = denylistItem!!.name
                pattern = denylistItem!!.pattern
            } else {
                name = intent.getStringExtra(PARAM_NAME)
                pattern = intent.getStringExtra(PARAM_NUMBER_PATTERN)
            }

            if (!TextUtils.isEmpty(pattern)) {
                pattern = DenylistUtils.patternToHumanReadable(pattern)
            }

            setString(nameTextField, name)
            setString(patternTextField, pattern)
        }

        val statsTextView = findViewById<TextView>(R.id.stats)
        if (denylistItem != null) {
            val statsString: String
            if (denylistItem!!.numberOfCalls > 0) {
                val dateFormat = DateFormat.getMediumDateFormat(this)
                val timeFormat = DateFormat.getTimeFormat(this)

                val lastCallDate = denylistItem!!.lastCallDate
                val dateString = if (lastCallDate != null)
                    (dateFormat.format(lastCallDate) + ' '
                            + timeFormat.format(lastCallDate))
                else
                    getString(R.string.denylist_item_date_no_info)

                statsString = resources.getQuantityString(
                    R.plurals.denylist_item_stats,
                    Math.toIntExact(denylistItem!!.numberOfCalls),
                    denylistItem!!.numberOfCalls,
                    dateString
                )
            } else {
                statsString = getString(R.string.denylist_item_no_calls)
            }
            statsTextView.text = statsString
        } else {
            statsTextView.visibility = View.GONE
        }

        val contactsNoticeTextView = findViewById<TextView>(R.id.contactsNotBlockedNotice)
        if (App.getSettings().useContacts) {
            if (!PermissionHelper.hasContactsPermission(this)) {
                contactsNoticeTextView.setText(R.string.contacts_are_not_blocked_no_permission)
            }
        } else {
            contactsNoticeTextView.setText(R.string.contacts_are_not_blocked_not_enabled)
        }

        patternTextField.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_denylist_item, menu)

        if (denylistItem == null) {
            menu.findItem(R.id.menu_delete).setVisible(false)
        }

        return true
    }

    fun onSaveClicked(item: MenuItem?) {
        if (validate()) {
            save()
            finish()
        }
    }

    fun onDeleteClicked(item: MenuItem?) {
        denylistService.delete(listOf(denylistItem!!.id))
        finish()
    }

    private fun validate(): Boolean {
        var pattern = getString(patternTextField)
        var valid = true
        val empty = TextUtils.isEmpty(pattern)

        if (denylistItem != null || !empty) {
            pattern = DenylistUtils.cleanPattern(DenylistUtils.patternFromHumanReadable(pattern))
            valid = DenylistUtils.isValidPattern(pattern)
        }

        patternTextField.error = if (!valid) {
            getString(
                if (empty) R.string.number_pattern_empty else R.string.number_pattern_incorrect
            )
        } else {
            null
        }

        return valid
    }

    private fun save() {
        val name = getString(nameTextField)
        val pattern = DenylistUtils.cleanPattern(
            DenylistUtils.patternFromHumanReadable(getString(patternTextField))
        )
        val invalid = !DenylistUtils.isValidPattern(pattern)

        if (denylistItem != null) {
            var changed = false

            val updatedItem = denylistItem!!.copy(
                name = if (!TextUtils.equals(name, denylistItem!!.name)) {
                    changed = true
                    name
                } else {
                    denylistItem!!.name
                },
                pattern = if (!TextUtils.equals(pattern, denylistItem!!.pattern)) {
                    changed = true
                    pattern
                } else {
                    denylistItem!!.pattern
                },
                invalid = if (invalid != denylistItem!!.invalid) {
                    changed = true
                    invalid
                } else {
                    denylistItem!!.invalid
                }
            )

            if (changed) {
                denylistService.update(updatedItem)
            }
        } else {
            if (name.isEmpty() || pattern.isNullOrEmpty()) {
                LOG.info("save() not creating a new item because fields are empty")
                return
            }

            if (runBlocking { denylistDao.findByNameAndPattern(name, pattern) } != null) {
                LOG.info(
                    "save() not creating a new item because" +
                            " an item with the same name and pattern exists"
                )
                return
            }

            val denylistItem = DenylistItem(
                id = -1L,
                name = name,
                pattern = pattern,
                creationDate = Date(),
                invalid = false,
                numberOfCalls = 0,
                lastCallDate = null
            )
            denylistService.insert(denylistItem)
        }
    }

    private fun getString(textInputLayout: TextInputLayout): String {
        return textInputLayout.editText!!.text.toString()
    }

    private fun setString(textInputLayout: TextInputLayout, s: String?) {
        textInputLayout.editText!!.setText(s)
    }

    companion object {
        private const val PARAM_ITEM_ID = "itemId"
        private const val PARAM_NAME = "itemName"
        private const val PARAM_NUMBER_PATTERN = "numberPattern"

        private val LOG: Logger = LoggerFactory.getLogger(
            EditDenylistItemActivity::class.java
        )

        fun getIntent(context: Context?, itemId: Long): Intent {
            val intent = Intent(
                context,
                EditDenylistItemActivity::class.java
            )
            intent.putExtra(PARAM_ITEM_ID, itemId)
            return intent
        }

        @JvmStatic
        fun getIntent(context: Context?, name: String?, numberPattern: String?): Intent {
            val intent = Intent(
                context,
                EditDenylistItemActivity::class.java
            )
            intent.putExtra(PARAM_NAME, name)
            intent.putExtra(PARAM_NUMBER_PATTERN, numberPattern)
            return intent
        }
    }
}
