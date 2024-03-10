package fr.vinetos.tranquille;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import fr.vinetos.tranquille.data.YacbHolder;
import fr.vinetos.tranquille.event.MainDbDownloadFinishedEvent;
import fr.vinetos.tranquille.event.SecondaryDbUpdateFinished;
import fr.vinetos.tranquille.event.SecondaryDbUpdatingEvent;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import fr.vinetos.tranquille.work.TaskService;

public class AboutActivity extends AppCompatActivity {

    private final Settings settings = App.getSettings();
    private final CommunityDatabase communityDatabase = YacbHolder.getCommunityDatabase();

    private TextView dbInfoTv;

    private boolean checkingForUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setLink(R.id.homepage, R.string.url_repo, R.string.homepage);
        setLink(R.id.faq, R.string.url_faq, R.string.faq);
        setLink(R.id.translate, R.string.url_translate, R.string.translate);
        setLink(R.id.issues, R.string.url_issues, R.string.issues);

        ((TextView) findViewById(R.id.app_version)).setText(
                getString(R.string.version_string, BuildConfig.VERSION_NAME));

        dbInfoTv = findViewById(R.id.db_info);

        dbInfoTv.setOnLongClickListener(this::onDbInfoLongClicked);

        if (EventUtils.bus().getStickyEvent(SecondaryDbUpdatingEvent.class) != null) {
            checkingForUpdates = true;
        }

        updateDbInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventUtils.register(this);
    }

    @Override
    protected void onStop() {
        EventUtils.unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMainDbDownloadFinished(MainDbDownloadFinishedEvent event) {
        updateDbInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onSecondaryDbUpdating(SecondaryDbUpdatingEvent event) {
        checkingForUpdates = true;
        updateDbInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onSecondaryDbUpdateFinished(SecondaryDbUpdateFinished event) {
        checkingForUpdates = false;
        updateDbInfo();
    }

    private void updateDbInfo() {
        // TODO: async?

        boolean clickable;

        String dbVersionValue;
        if (communityDatabase.isOperational()) {
            dbVersionValue = String.valueOf(communityDatabase.getEffectiveDbVersion());
            clickable = true;
        } else {
            dbVersionValue = getString(R.string.db_version_not_available);
            clickable = false;
        }

        if (clickable && checkingForUpdates) clickable = false;

        setUpdateClickable(clickable);

        String lastCheckValue;
        if (checkingForUpdates) {
            lastCheckValue = getString(R.string.db_last_update_check_checking);
        } else {
            long lastUpdateCheckTime = settings.getLastUpdateCheckTime();
            lastCheckValue = lastUpdateCheckTime != 0 ?
                    DateUtils.getRelativeTimeSpanString(lastUpdateCheckTime).toString()
                    : getString(R.string.db_last_update_check_never);
        }

        String dbInfoString = getString(R.string.db_version, dbVersionValue)
                + "\n" + getString(R.string.db_last_update_check, lastCheckValue);

        dbInfoTv.setText(dbInfoString);
    }

    private void setUpdateClickable(boolean clickable) {
        dbInfoTv.setClickable(clickable);
    }

    public void onUpdateDbClicked(View view) {
        setUpdateClickable(false);

        TaskService.start(this, TaskService.TASK_UPDATE_SECONDARY_DB);
    }

    private boolean onDbInfoLongClicked(View view) {
        startActivity(new Intent(this, DbManagementActivity.class));
        finish();
        return true;
    }

    private void setLink(@IdRes int textView, @StringRes int url, @StringRes int text) {
        setLink(findViewById(textView), getString(url), getString(text));
    }

    private void setLink(TextView textView, String url, String text) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(fromHtml("<a href=\"" + url + "\">" + text + "</a>"));
    }

    private static Spanned fromHtml(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return fromHtmlLegacy(s);
        }
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private static Spanned fromHtmlLegacy(String s) {
        return Html.fromHtml(s);
    }

}
