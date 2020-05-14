package dummydomain.yetanothercallblocker.sia.model.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import dummydomain.yetanothercallblocker.sia.Storage;
import dummydomain.yetanothercallblocker.sia.network.DbDownloader;
import dummydomain.yetanothercallblocker.sia.utils.FileUtils;

public class DbManager {

    private static final Logger LOG = LoggerFactory.getLogger(DbManager.class);

    private static final String DEFAULT_URL = "https://gitlab.com/xynngh/YetAnotherCallBlocker_data/raw/zip_v1/archives/sia.zip";

    private final Storage storage;
    private final String pathPrefix;

    public DbManager(Storage storage, String pathPrefix) {
        this.storage = storage;
        this.pathPrefix = pathPrefix;
    }

    public boolean downloadMainDb() {
        return downloadMainDb(DEFAULT_URL);
    }

    public boolean downloadMainDb(String url) {
        LOG.debug("downloadMainDb() started");

        File dataDir = new File(storage.getDataDirPath());

        String siaDir = pathPrefix;
        String tmpUpdateDir = siaDir.substring(0, siaDir.indexOf('/')) + "-tmp/";
        String oldDir = siaDir.substring(0, siaDir.indexOf('/')) + "-old/";

        FileUtils.delete(dataDir, tmpUpdateDir);
        FileUtils.createDirectory(dataDir, tmpUpdateDir);
        LOG.debug("downloadMainDb() prepared dirs");

        if (DbDownloader.download(url, storage.getDataDirPath() + tmpUpdateDir)) {
            LOG.debug("downloadMainDb() downloaded and unpacked");

            File old = new File(dataDir, siaDir);
            if (old.exists() && !old.renameTo(new File(dataDir, oldDir))) {
                LOG.warn("downloadMainDb() couldn't rename sia to old");
                return false;
            }

            if (!new File(dataDir, tmpUpdateDir).renameTo(new File(dataDir, siaDir))) {
                LOG.warn("downloadMainDb() couldn't rename tmp to sia");
                return false;
            }

            FileUtils.delete(dataDir, oldDir);

            LOG.debug("downloadMainDb() folders moved");
            return true;
        } else {
            LOG.warn("downloadMainDb() failed downloading");
        }

        return false;
    }

}
