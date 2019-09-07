package dummydomain.yetanothercallblocker.sia.model.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import dummydomain.yetanothercallblocker.sia.SiaConstants;
import dummydomain.yetanothercallblocker.sia.network.DbDownloader;
import dummydomain.yetanothercallblocker.sia.utils.FileUtils;

public class DbManager {

    private static final Logger LOG = LoggerFactory.getLogger(DbManager.class);

    public static boolean downloadMainDb() {
        String url = "https://gitlab.com/xynngh/YetAnotherCallBlocker_data/raw/zip_v1/archives/sia.zip";
        return downloadMainDb(url);
    }

    public static boolean downloadMainDb(String url) {
        LOG.debug("downloadMainDb() started");

        File dataDir = FileUtils.getDataDir();

        String siaDir = SiaConstants.SIA_PATH_PREFIX;
        String tmpUpdateDir = siaDir.substring(0, siaDir.indexOf('/')) + "-tmp/";
        String oldDir = siaDir.substring(0, siaDir.indexOf('/')) + "-old/";

        FileUtils.delete(dataDir, tmpUpdateDir);
        FileUtils.createDirectory(dataDir, tmpUpdateDir);
        LOG.debug("downloadMainDb() prepared dirs");

        if (DbDownloader.download(url, FileUtils.getDataDirPath() + tmpUpdateDir)) {
            LOG.debug("downloadMainDb() downloaded and unpacked");
            new File(dataDir, siaDir).renameTo(new File(dataDir, oldDir));
            new File(dataDir, tmpUpdateDir).renameTo(new File(dataDir, siaDir));
            FileUtils.delete(dataDir, oldDir);
            LOG.debug("downloadMainDb() folders moved");
            return true;
        } else {
            LOG.warn("downloadMainDb() failed downloading");
        }

        return false;
    }

}
