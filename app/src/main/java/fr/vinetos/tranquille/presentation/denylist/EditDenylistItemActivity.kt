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
        nameEditText = findViewById<TextInputEditText?>(R.id.nameEditText).apply {
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
                    validateName(s)
                }
            })
        }

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
        validateName(null)

        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_edit_blacklist_item, menu)

        if (denylistItem == null) {
            menu?.findItem(R.id.menu_delete)?.isVisible = false
        }

        // Hide save menu item if the pattern or the name is not valid
        menuSaveItem = menu?.findItem(R.id.menu_save)
        val pattern = validatePattern(null)
        val name = validateName(null)
        menuSaveItem?.isVisible = pattern && name
        menuSaveItem?.setOnMenuItemClickListener {
            // We are creating an item
            if(denylistItem == null) {
                // TODO: Make the service does all the inputs checks
                // and find a way to not build a DenylistItem object
                // Probably an entity ?
                denylistService.insert(DenylistItem(
                    0,
                    nameEditText.text.toString(),
                    patternEditText.text.toString(),
                    "why it exists",
                    Date().toString(),
                    0,
                    0,
                    Date().toString()
                ))
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
     * Validate the name and show an error if it's not valid
     */
    fun validateName(s: Editable?): Boolean {
        val name = s?.toString() ?: nameEditText.text.toString()

        if (name.isEmpty())
            nameTextLayout.error = getString(R.string.denylist_item_name_empty)
        else
            nameTextLayout.error = null

        // Hide save menu item if the pattern or the name is not valid
        updateSaveButton()

        return nameTextLayout.error == null
    }

    /**
     * Update the visibility of the save button in the menu
     * depending on the validity of the name and the pattern
     */
    private fun updateSaveButton() {
        // Hide save menu item if the name or pattern is not valid
        // Var are defined to call both methods
        val pattern = patternTextLayout.error == null
        val name = patternTextLayout.error == null
        menuSaveItem?.isVisible = pattern && name
    }


    /**
     * Mark a field as required by adding a star to the hint
     */
    private fun TextInputLayout.markRequired() {
        hint = "$hint *"
    }

}