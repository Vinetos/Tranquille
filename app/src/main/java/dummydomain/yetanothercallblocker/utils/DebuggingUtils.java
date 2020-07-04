package dummydomain.yetanothercallblocker.utils;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dummydomain.yetanothercallblocker.App;

public class DebuggingUtils {

    private static final String TAG = DebuggingUtils.class.getSimpleName();

    public static void setUpCrashHandler() {
        Thread.UncaughtExceptionHandler defaultHandler
                = Thread.getDefaultUncaughtExceptionHandler();

        Thread.UncaughtExceptionHandler customHandler = (t, e) -> {
            try {
                handleCrash(e);
            } finally {
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(t, e);
                }
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(customHandler);
    }

    private static void handleCrash(Throwable e) {
        boolean logToExternal;
        try {
            logToExternal = App.getSettings().getSaveCrashesToExternalStorage();
        } catch (Exception ignored) {
            logToExternal = false;
        }

        try {
            saveCrashToFile(App.getInstance(), e, logToExternal);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        boolean saveLogcat;
        try {
            saveLogcat = App.getSettings().getSaveLogcatOnCrash();
        } catch (Exception ignored) {
            saveLogcat = false;
        }

        if (saveLogcat) {
            try {
                saveLogcatToFile(App.getInstance(), logToExternal);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void saveCrashToFile(Context context, Throwable th, boolean external)
            throws IOException {
        saveCrashToFile(context, th, external, getDateString());
    }

    public static void saveLogcatToFile(Context context, boolean external) throws IOException {
        saveLogcatToFile(context, external, getDateString());
    }

    private static void saveCrashToFile(Context context, Throwable th,
                                        boolean external, String name)
            throws IOException {
        String fileName = getFilesDir(context, external).getAbsolutePath()
                + "/crash_" + name + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            th.printStackTrace(writer);
        }
    }

    private static void saveLogcatToFile(Context context, boolean external, String name)
            throws IOException {
        String path = getFilesDir(context, external).getAbsolutePath()
                + "/logcat_" + name + ".txt";

        Log.d(TAG, "Saving logcat to " + path);
        Runtime.getRuntime().exec("logcat -d -f " + path);
    }

    private static String getDateString() {
        return new SimpleDateFormat("yyyyMMdd_HHmmssS", Locale.US).format(new Date());
    }

    private static File getFilesDir(Context context, boolean external) {
        if (external) {
            File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
            for (File dir : dirs) {
                if (dir != null) return dir;
            }

            Log.d(TAG, "getFilesDir() no external dirs available");
        }

/*
        not secure enough
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!context.isDeviceProtectedStorage()) {
                context = context.createDeviceProtectedStorageContext();
            }
        }
*/

        return context.getFilesDir();
    }

}
