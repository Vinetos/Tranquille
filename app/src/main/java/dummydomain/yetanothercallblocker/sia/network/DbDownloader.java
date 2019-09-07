package dummydomain.yetanothercallblocker.sia.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dummydomain.yetanothercallblocker.sia.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Okio;
import okio.Sink;

public class DbDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(DbDownloader.class);

    public static boolean download(String url, String path) {
        LOG.info("download() started; path: {}", path);

        LOG.debug("download() making a request");
        Request request = new Request.Builder().url(url).build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            LOG.debug("download() got response; successful: {}", response.isSuccessful());
            if (response.isSuccessful()) {
                ResponseBody body = response.body();

                try (ZipInputStream zipInputStream = new ZipInputStream(body.byteStream())) {
                    for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                        String name = zipEntry.getName();

                        if (zipEntry.isDirectory()) {
                            FileUtils.createDirectory(path + name);
                            continue;
                        }

                        try (Sink out = Okio.sink(new File(path + name))) {
                            Okio.buffer(Okio.source(zipInputStream)).readAll(out);
                        }
                    }

                    LOG.debug("download() finished successfully");
                    return true;
                }
            } else {
                LOG.warn("download() unsuccessful response {}", response.message());
            }
        } catch (IOException e) {
            LOG.warn("download()", e);
        }

        LOG.debug("download() finished unsuccessfully");
        return false;
    }

}
