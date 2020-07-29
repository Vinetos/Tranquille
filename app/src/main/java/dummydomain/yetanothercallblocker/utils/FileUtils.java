package dummydomain.yetanothercallblocker.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileUtils {

    private static final String FILEPROVIDER_AUTHORITY = "dummydomain.yetanothercallblocker.fileprovider";

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static void shareFile(Activity activity, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(activity, FILEPROVIDER_AUTHORITY, file);

            ShareCompat.IntentBuilder shareBuilder = ShareCompat.IntentBuilder.from(activity)
                    .setStream(uri)
                    .setType(activity.getContentResolver().getType(uri));

            shareBuilder.getIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            shareBuilder.startChooser();
        } catch (Exception e) {
            LOG.warn("shareFile()", e);
        }
    }

}
