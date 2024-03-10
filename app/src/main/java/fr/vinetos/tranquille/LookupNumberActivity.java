package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import fr.vinetos.tranquille.data.SiaNumberCategoryUtils;
import fr.vinetos.tranquille.data.YacbHolder;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class LookupNumberActivity extends AppCompatActivity {

    private static final String[] MIME_TYPES = new String[]{
            ClipDescription.MIMETYPE_TEXT_PLAIN,
            "vnd.android.cursor.item/phone_v2" // ContactsContract.Phone.CONTENT_ITEM_TYPE
    };

    private Settings settings;

    private ClipboardManager clipboardManager;

    private EditText phoneNumberInput;
    private View reviewsSummary;
    private TextView reviewsPhoneNumber, reviewsDetails;

    private AsyncTask<String, Void, Pair<CommunityDatabaseItem, FeaturedDatabaseItem>> queryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup_number);

        settings = App.getSettings();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        reviewsSummary = findViewById(R.id.reviews_summary);
        reviewsPhoneNumber = findViewById(R.id.reviews_phone_number);
        reviewsDetails = findViewById(R.id.reviews_details);

        hideReviewSummary();

        if (savedInstanceState == null) {
            String numberFromClipboard = getNumberFromClipboard();
            if (!cleanNumber(numberFromClipboard).isEmpty()) {
                phoneNumberInput.setText(numberFromClipboard);
            }

            postInit();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        postInit();
    }

    private void postInit() {
        if (!getPureNumber().isEmpty()) {
            onQueryDbButtonClick(null);
        }

        phoneNumberInput.requestFocus();
    }

    @Override
    protected void onDestroy() {
        cancelQueryTask();

        super.onDestroy();
    }

    private String getPureNumber() {
        return cleanNumber(phoneNumberInput.getText().toString());
    }

    private String cleanNumber(String rawNumber) {
        if (!TextUtils.isEmpty(rawNumber)) {
            String pureNumber = rawNumber.replaceAll("[^\\d]", "");
            if (!TextUtils.isEmpty(pureNumber)) {
                if (pureNumber.charAt(0) == '8' && pureNumber.length() == 11) {
                    // a hack for Russian numbers
                    if ("RU".equals(settings.getCachedAutoDetectedCountryCode())) {
                        pureNumber = "7".concat(pureNumber.substring(1));
                    }
                }
                return pureNumber;
            }
        }
        return "";
    }

    private void clearOutput() {
        setReviewsNumberAndDetails("", "");
        hideReviewSummary();
    }

    private void setReviewsNumberAndDetails(String number, String details) {
        reviewsPhoneNumber.setText(number);
        reviewsPhoneNumber.setVisibility(TextUtils.isEmpty(number) ? View.GONE : View.VISIBLE);

        reviewsDetails.setText(details);
        reviewsDetails.setVisibility(TextUtils.isEmpty(details) ? View.GONE : View.VISIBLE);
    }

    private void hideReviewSummary() {
        reviewsSummary.setVisibility(View.GONE);
    }

    private void displayNumberSummary(CommunityDatabaseItem item) {
        ReviewsSummaryHelper.populateSummary(reviewsSummary, item);
    }

    private boolean isWrongNumberInput() {
        if (getPureNumber().isEmpty()) {
            phoneNumberInput.setError(getString(R.string.lookup_error_not_a_number));
            return true;
        }
        return false;
    }

    public void onLoadReviewsButtonClick(View view) {
        if (isWrongNumberInput()) return;

        ReviewsActivity.startForNumber(this, getPureNumber());
    }

    public void onAddReviewButtonClick(View view) {
        if (isWrongNumberInput()) return;

        Uri uri = Uri.parse(YacbHolder.getWebService().getWebReviewsUrlPart() + getPureNumber());
        IntentHelper.startActivity(this, new Intent(Intent.ACTION_VIEW, uri));
    }

    public void onClearNumberButtonClick(View view) {
        phoneNumberInput.setText("");
        clearOutput();
        phoneNumberInput.requestFocus();
    }

    public void onPasteNumberButtonClick(View view) {
        if (pasteNumberFromClipboard()) {
            onQueryDbButtonClick(null);
        }
    }

    private boolean pasteNumberFromClipboard() {
        String number = getNumberFromClipboard();
        if (!TextUtils.isEmpty(number)) {
            phoneNumberInput.setText(number);
            return true;
        }
        return false;
    }

    private String getNumberFromClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();
            if (clipDescription != null && hasMimeType(clipDescription, MIME_TYPES)) {
                ClipData primaryClip = clipboardManager.getPrimaryClip();
                if (primaryClip != null) {
                    ClipData.Item item = primaryClip.getItemAt(0);
                    return item.getText().toString();
                }
            }
        }
        return "";
    }

    public void onQueryDbButtonClick(View view) {
        clearOutput();
        if (isWrongNumberInput()) return;

        startQueryTask(getPureNumber());
    }

    private void startQueryTask(String number) {
        cancelQueryTask();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Void, Pair<CommunityDatabaseItem, FeaturedDatabaseItem>> task = this.queryTask
                = new AsyncTask<String, Void, Pair<CommunityDatabaseItem, FeaturedDatabaseItem>>() {
            @Override
            protected Pair<CommunityDatabaseItem, FeaturedDatabaseItem> doInBackground(String... params) {
                String purePhoneNumber = params[0];
                CommunityDatabaseItem item = YacbHolder.getCommunityDatabase()
                        .getDbItemByNumber(purePhoneNumber);

                FeaturedDatabaseItem featuredItem = YacbHolder.getFeaturedDatabase()
                        .getDbItemByNumber(purePhoneNumber);

                return new Pair<>(item, featuredItem);
            }

            @Override
            protected void onPostExecute(Pair<CommunityDatabaseItem, FeaturedDatabaseItem> result) {
                onQueryResult(result.first, result.second);
            }
        };
        task.execute(number);
    }

    private void cancelQueryTask() {
        if (queryTask != null) {
            queryTask.cancel(true);
            queryTask = null;
        }
    }

    private void onQueryResult(CommunityDatabaseItem item, FeaturedDatabaseItem featuredItem) {
        String number = "", details = "";

        if (item == null) {
            details = getString(R.string.lookup_number_not_found);
        } else {
            number = '+' + String.valueOf(item.getNumber());

            if (BuildConfig.DEBUG && item.getUnknownData() != 0) {
                details += "Unknown data: " + item.getUnknownData() + '\n';
            }

            NumberCategory category = NumberCategory.getById(item.getCategory());
            if (category != null) {
                details += SiaNumberCategoryUtils.getName(this, category) + '\n';
            } else {
                details += getString(R.string.lookup_res_category, item.getCategory()) + '\n';
            }

            displayNumberSummary(item);
        }

        if (featuredItem != null) {
            details += getString(R.string.lookup_res_featured_name, featuredItem.getName());
        }

        setReviewsNumberAndDetails(number, details);
    }

    private static boolean hasMimeType(ClipDescription cd, String... mimeTypes) {
        for (String mimeType : mimeTypes) {
            if (cd.hasMimeType(mimeType)) {
                return true;
            }
        }
        return false;
    }

}
