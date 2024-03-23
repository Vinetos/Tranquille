package fr.vinetos.tranquille;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyDialerActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(DummyDialerActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        LOG.info("onCreate() intent: {}", intent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return; // not applicable to earlier versions
        }

        intent.setComponent(null);

        ActivityInfo found = null;

        for (ResolveInfo info : getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_ALL)) {
            ActivityInfo activityInfo = info.activityInfo;
            if (activityInfo != null && activityInfo.applicationInfo.enabled
                    && !activityInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                LOG.debug("onCreate() found match: {}", activityInfo);
                found = activityInfo;
                break; // should explicitly prefer default dialer (by packageName)?
            }
        }

        if (found != null) {
            intent.setComponent(new ComponentName(found.applicationInfo.packageName, found.name));
            startActivity(intent);
        } else {
            LOG.error("onCreate() didn't find any dialer to launch");
        }

        finish();
    }

}
