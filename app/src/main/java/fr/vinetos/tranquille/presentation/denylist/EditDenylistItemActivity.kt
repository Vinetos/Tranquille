package fr.vinetos.tranquille.presentation.denylist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.domain.service.DenylistService
import fr.vinetos.tranquille.data.BlacklistUtils
import fr.vinetos.tranquille.data.DenylistItem
import fr.vinetos.tranquille.data.YacbHolder
import java.time.ZonedDateTime
import java.util.Date

class EditDenylistItemActivity : AppCompatActivity() {

    private var denylistItem: DenylistItem? = null
    lateinit var nameTextLayout: TextInputLayout
    lateinit var nameEditText: TextInputEditText
    lateinit var patternTextLayout: TextInputLayout
    lateinit var patternEditText: TextInputEditText
    private var menuSaveItem: MenuItem? = null
    private var denylistService: DenylistService = YacbHolder.getBlacklistService()

    // todo: This activity is called when creating a deny list and when editing a deny list item.
    // Change title by title_add_blacklist_item_activity or title_edit_blacklist_item_activity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_blacklist_item)

        nameTextLayout = findViewById(R.id.nameTextField)
        nameTextLayout.markRequired()
        nameEditText = findViewById(R.id.nameEditText)

        patternTextLayout = findViewById(R.id.patternTextField)
        patternTextLayout.markRequired()

        patternEditText = findViewById<TextInputEditText?>(R.id.patternEditText).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    validatePattern(s)
                }
            })
        }

        validatePattern(null)

        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_edit_blacklist_item, menu)

        if (denylistItem == null) {
            menu?.findItem(R.id.menu_delete)?.isVisible = false
        }

        // Hide save menu item if the pattern or the name is not valid
        menuSaveItem = menu?.findItem(R.id.menu_save)
        menuSaveItem?.isVisible = validatePattern(null)
        menuSaveItem?.setOnMenuItemClickListener {
            // We are creating an item
            if (denylistItem == null) {
                val res = denylistService.insert(
                    nameEditText.text.toString(),
                    patternEditText.text.toString(),
                )
                if (res)
                    finish()
            }

            // Consume the click
            true
        }

        return true
    }

    /**
     * Validate the pattern and show an error if it's not valid
     */
    fun validatePattern(s: Editable?): Boolean {
        val pattern = s?.toString() ?: patternEditText.text.toString()

        if (pattern.isEmpty())
            patternTextLayout.error = getString(R.string.number_pattern_empty)
        else if (!BlacklistUtils.isValidPattern(pattern))
            patternTextLayout.error = getString(R.string.number_pattern_incorrect)
        else
            patternTextLayout.error = null

        // Hide save menu item if the pattern or the name is not valid
        updateSaveButton()

        return patternTextLayout.error == null
    }

    /**
     * Update the visibility of the save button in the menu
     * depending on the validity of the name and the pattern
     */
    private fun updateSaveButton() {
        // Hide save menu item if the pattern is not valid
        menuSaveItem?.isVisible = patternTextLayout.error == null
    }


    /**
     * Mark a field as required by adding a star to the hint
     */
    private fun TextInputLayout.markRequired() {
        hint = "$hint *"
    }

}