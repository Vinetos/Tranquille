package fr.vinetos.tranquille.presentation.denylist

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import fr.vinetos.tranquille.R

class DenylistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_blacklist)

        // Load the adapter with the denylist items
        // Remove the dataclass DenylistItem and use the data from the database
        TODO("Not implemented")
    }

}