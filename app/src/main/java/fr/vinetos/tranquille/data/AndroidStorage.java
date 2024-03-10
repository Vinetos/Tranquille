package fr.vinetos.tranquille.data;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import dummydomain.yetanothercallblocker.sia.Storage;

public class AndroidStorage implements Storage {

    private final Context context;

    public AndroidStorage(Context context) {
        this.context = context;
    }

    @Override
    public String getDataDirPath() {
        return context.getFilesDir().getAbsolutePath() + "/";
    }

    @Override
    public String getCacheDirPath() {
        return context.getCacheDir().getAbsolutePath() + "/";
    }

    @Override
    public InputStream openFile(String fileName, boolean internal) throws IOException {
        if (internal) {
            return context.getAssets().open(fileName);
        } else {
            return new FileInputStream(getDataDirPath() + fileName);
        }
    }

}
