package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

import dummydomain.yetanothercallblocker.data.BlacklistService;
import dummydomain.yetanothercallblocker.data.BlacklistUtils;
import dummydomain.yetanothercallblocker.data.YacbHolder;
import dummydomain.yetanothercallblocker.data.db.BlacklistDao;
import dummydomain.yetanothercallblocker.data.db.BlacklistItem;

import static dummydomain.yetanothercallblocker.data.BlacklistUtils.cleanPattern;
import static dummydomain.yetanothercallblocker.data.BlacklistUtils.patternFromHumanReadable;

public class EditBlacklistItemActivity extends AppCompatActivity {

    private static final String PARAM_ITEM_ID = "itemId";
    private static final String PARAM_NAME = "itemName";
    private static final String PARAM_NUMBER_PATTERN = "numberPattern";

    private static final Logger LOG = LoggerFactory.getLogger(EditBlacklistItemActivity.class);

    private BlacklistDao blacklistDao = YacbHolder.getBlacklistDao();
    private BlacklistService blacklistService = YacbHolder.getBlacklistService();

    private TextInputLayout nameTextField;
    private TextInputLayout patternTextField;

    private BlacklistItem blacklistItem;

    public static Intent getIntent(Context context, long itemId) {
        Intent intent = new Intent(context, EditBlacklistItemActivity.class);
        intent.putExtra(PARAM_ITEM_ID, itemId);
        return intent;
    }

    public static Intent getIntent(Context context, String name, String numberPattern) {
        Intent intent = new Intent(context, EditBlacklistItemActivity.class);
        intent.putExtra(PARAM_NAME, name);
        intent.putExtra(PARAM_NUMBER_PATTERN, numberPattern);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_blacklist_item);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        nameTextField = findViewById(R.id.nameTextField);
        patternTextField = findViewById(R.id.patternTextField);

        EditText patternEditText = Objects.requireNonNull(patternTextField.getEditText());
        patternEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });
        patternEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onSaveClicked(null);
                        return true;
                    }
                    return false;
                }
        );

        long itemIdFromParams = getIntent().getLongExtra(PARAM_ITEM_ID, -1);
        if (itemIdFromParams != -1) {
            blacklistItem = blacklistDao.findById(itemIdFromParams);
            if (blacklistItem == null) {
                LOG.warn("onCreate() no item with id={}", itemIdFromParams);
                finish();
                return;
            }

            setTitle(R.string.title_edit_blacklist_item_activity);
        }

        if (savedInstanceState == null) {
            String name;
            String pattern;

            if (blacklistItem != null) {
                name = blacklistItem.getName();
                pattern = blacklistItem.getPattern();
            } else {
                name = getIntent().getStringExtra(PARAM_NAME);
                pattern = getIntent().getStringExtra(PARAM_NUMBER_PATTERN);
            }

            if (!TextUtils.isEmpty(pattern)) {
                pattern = BlacklistUtils.patternToHumanReadable(pattern);
            }

            setString(nameTextField, name);
            setString(patternTextField, pattern);
        }

        patternTextField.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_blacklist_item, menu);

        if (blacklistItem == null) {
            menu.findItem(R.id.menu_delete).setVisible(false);
        }

        return true;
    }

    public void onSaveClicked(MenuItem item) {
        if (validate()) {
            save();
            finish();
        }
    }

    public void onDeleteClicked(MenuItem item) {
        blacklistService.delete(Collections.singletonList(blacklistItem.getId()));
        finish();
    }

    private boolean validate() {
        String pattern = getString(patternTextField);
        boolean valid = true;
        boolean empty = TextUtils.isEmpty(pattern);

        if (blacklistItem != null || !empty) {
            pattern = cleanPattern(patternFromHumanReadable(pattern));
            valid = BlacklistUtils.isValidPattern(pattern);
        }

        patternTextField.setError(!valid ? getString(
                empty ? R.string.number_pattern_empty : R.string.number_pattern_incorrect)
                : null);

        return valid;
    }

    private void save() {
        String name = getString(nameTextField);
        String pattern = cleanPattern(patternFromHumanReadable(getString(patternTextField)));
        boolean invalid = !BlacklistUtils.isValidPattern(pattern);

        if (blacklistItem != null) {
            boolean changed = false;
            if (!TextUtils.equals(name, blacklistItem.getName())) {
                blacklistItem.setName(name);
                changed = true;
            }
            if (!TextUtils.equals(pattern, blacklistItem.getPattern())) {
                blacklistItem.setPattern(pattern);
                changed = true;
            }
            if (invalid != blacklistItem.getInvalid()) {
                changed = true;
            }

            if (changed) {
                blacklistService.save(blacklistItem);
            }
        } else {
            if (TextUtils.isEmpty(name) && TextUtils.isEmpty(pattern)) {
                LOG.warn("save() not creating a new item because fields are empty");
                return;
            }

            BlacklistItem blacklistItem = new BlacklistItem(name, pattern);
            blacklistService.save(blacklistItem);
        }
    }

    private String getString(TextInputLayout textInputLayout) {
        return Objects.requireNonNull(textInputLayout.getEditText()).getText().toString();
    }

    private void setString(TextInputLayout textInputLayout, String s) {
        Objects.requireNonNull(textInputLayout.getEditText()).setText(s);
    }

}
