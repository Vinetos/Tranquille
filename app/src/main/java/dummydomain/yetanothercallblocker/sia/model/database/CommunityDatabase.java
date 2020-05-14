package dummydomain.yetanothercallblocker.sia.model.database;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import dummydomain.yetanothercallblocker.sia.network.WebService;
import dummydomain.yetanothercallblocker.sia.utils.FileUtils;
import dummydomain.yetanothercallblocker.sia.utils.Utils;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static dummydomain.yetanothercallblocker.sia.utils.FileUtils.getDataDir;
import static dummydomain.yetanothercallblocker.sia.utils.FileUtils.getDataDirPath;
import static dummydomain.yetanothercallblocker.sia.utils.FileUtils.openFile;

public class CommunityDatabase extends AbstractDatabase<CommunityDatabaseDataSlice, CommunityDatabaseItem> {

    private enum UpdateResult {
        UPDATED, NO_UPDATES, OUTDATED_APP, BAD_SECONDARY, UNKNOWN_ERROR
    }

    private static final Logger LOG = LoggerFactory.getLogger(CommunityDatabase.class);

    private static final int FALLBACK_APP_VERSION = 114;

    private int siaAppVersion = FALLBACK_APP_VERSION;

    protected final String secondaryPathPrefix;

    private SparseArray<CommunityDatabaseDataSlice> secondarySliceCache = new SparseArray<>();

    @SuppressLint("UseSparseArrays") // uses null as a special value
    private SparseArray<Boolean> existingSecondarySliceFiles = new SparseArray<>();

    public CommunityDatabase(String pathPrefix, String secondaryPathPrefix) {
        super(pathPrefix);
        this.secondaryPathPrefix = secondaryPathPrefix;
    }

    public int getEffectiveDbVersion() {
        checkLoaded();

        int secondaryDbVersion = Utils.getSettings().getSecondaryDbVersion();
        return secondaryDbVersion > 0 ? secondaryDbVersion : baseDatabaseVersion;
    }

    public int getSiaAppVersion() {
        return siaAppVersion;
    }

    @Override
    protected String getNamePrefix() {
        return "data_slice_";
    }

    @Override
    protected void reset() {
        super.reset();

        siaAppVersion = FALLBACK_APP_VERSION;
        secondarySliceCache.clear();
        existingSecondarySliceFiles.clear();
    }

    @Override
    protected boolean load(boolean useAssets) {
        if (!super.load(useAssets)) return false;

        LOG.debug("load() started; useAssets={}", useAssets);

        String fileName = getPathPrefix() + "sia_info.dat";
        try (InputStream is = openFile(fileName, useAssets);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            if (!"SIA".equals(bufferedReader.readLine())) {
                LOG.debug("load() incorrect header");
                return false;
            }

            String appVersionString = bufferedReader.readLine();
            siaAppVersion = Integer.parseInt(appVersionString);

            LOG.debug("load() loaded extra info; siaAppVersion={}", this.siaAppVersion);
        } catch (Exception e) {
            LOG.debug("load() failed to load extra info", e);
        }

        return true;
    }

    @Override
    protected void loadInfoDataAfterLoadedHook(boolean useAssets) {
        int oldDbVersion = Utils.getSettings().getBaseDbVersion();
        if (baseDatabaseVersion != oldDbVersion) {
            LOG.info("loadInfoDataAfterLoadedHook() base version changed; resetting secondary DB;" +
                    " oldDbVersion={}, baseDatabaseVersion={}", oldDbVersion, baseDatabaseVersion);
            resetSecondaryDatabase();
            Utils.getSettings().setBaseDbVersion(baseDatabaseVersion);
        }
    }

    @Override
    protected CommunityDatabaseItem getDbItemByNumberInternal(long number) {
        LOG.debug("getDbItemByNumberInternal({}) started", number);

        CommunityDatabaseDataSlice secondarySlice = getSecondaryDataSlice(number);

        CommunityDatabaseItem communityDatabaseItem = secondarySlice != null
                ? secondarySlice.getDbItemByNumber(number) : null;

        if (communityDatabaseItem == null) {
            LOG.trace("getDbItemByNumberInternal() not found in secondary DB");
            CommunityDatabaseDataSlice baseSlice = getDataSlice(number);
            communityDatabaseItem = baseSlice != null ? baseSlice.getDbItemByNumber(number) : null;
        }

        LOG.trace("getDbItemByNumberInternal() communityDatabaseItem={}", communityDatabaseItem);

        if (communityDatabaseItem != null && !communityDatabaseItem.hasRatings()) {
            communityDatabaseItem = null;
        }

        return communityDatabaseItem;
    }

    @Override
    protected CommunityDatabaseDataSlice createDbDataSlice() {
        return new CommunityDatabaseDataSlice();
    }

    private CommunityDatabaseDataSlice getSecondaryDataSlice(long number) {
        LOG.debug("getSecondaryDataSlice({}) started", number);

        if (number <= 0) return null;

        String numberString = String.valueOf(number);
        if (numberString.length() < 2) return null;

        int sliceId = Integer.parseInt(numberString.substring(0, 2));
        LOG.trace("getSecondaryDataSlice() sliceId={}", sliceId);

        CommunityDatabaseDataSlice communityDatabaseDataSlice = secondarySliceCache.get(sliceId);
        if (communityDatabaseDataSlice == null) {
            LOG.trace("getSecondaryDataSlice() trying to load slice with sliceId={}", sliceId);

            communityDatabaseDataSlice = new CommunityDatabaseDataSlice();
            String path = getCachedSecondarySliceFilePath(sliceId);
            if (path != null) {
                LOG.trace("getSecondaryDataSlice() slice file exists, loading from: {}", path);
                loadSlice(communityDatabaseDataSlice, path, false);
            } else {
                LOG.trace("getSecondaryDataSlice() slice file doesn't exist");
            }
            secondarySliceCache.put(sliceId, communityDatabaseDataSlice);
        } else {
            LOG.trace("getSecondaryDataSlice() found slice in cache");
        }
        return communityDatabaseDataSlice;
    }

    @Nullable private String getCachedSecondarySliceFilePath(int id) {
        String path = getSecondarySliceFilePath(id);
        Boolean exists = existingSecondarySliceFiles.get(id, null);
        if (exists == null) {
            exists = new File(getDataDirPath() + path).exists();
            existingSecondarySliceFiles.put(id, exists);
        }
        return exists ? path : null;
    }

    private String getSecondarySliceFilePath(int id) {
        return getSecondaryDbPathPrefix() + id + ".sia";
    }

    public void resetSecondaryDatabase() {
        LOG.debug("resetSecondaryDatabase() started");

        File dir = new File(getDataDir(), getSecondaryDbPathPrefix());
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    LOG.warn("resetSecondaryDatabase() failed to delete secondary DB file: {}", file.getAbsolutePath());
                }
            }
        }

        secondarySliceCache.clear();
        existingSecondarySliceFiles.clear();
        Utils.getSettings().setSecondaryDbVersion(0);

        LOG.info("resetSecondaryDatabase() secondary DB was reset");
    }

    protected String getSecondaryDbPathPrefix() {
        return secondaryPathPrefix;
    }

    protected void createSecondaryDbDirectory() {
        FileUtils.createDirectory(getDataDir(), getSecondaryDbPathPrefix());
    }

    public boolean updateSecondaryDb() {
        LOG.info("updateSecondaryDb() started");

        if (!isOperational()) {
            LOG.warn("updateSecondaryDb() DB is not operational, update aborted");
            return false;
        }

        long startTimestamp = System.currentTimeMillis();

        boolean updated = false;

        for (int i = 0; i < 1000; i++) {
            UpdateResult result = updateSecondaryDbInternal();
            LOG.debug("updateSecondaryDb() internal update result: {}", result);
            if (result == UpdateResult.UPDATED) {
                updated = true;
                if (LOG.isTraceEnabled()) {
                    LOG.trace("updateSecondaryDb DB version after update: {}", getEffectiveDbVersion());
                }
            } else {
                break;
            }
        }

        if (updated) {
            LOG.info("updateSecondaryDb() new DB version: {}", getEffectiveDbVersion());
        }

        LOG.info("updateSecondaryDb() finished in {} ms", System.currentTimeMillis() - startTimestamp);

        return updated;
    }

    private UpdateResult updateSecondaryDbInternal() {
        LOG.debug("updateSecondaryDbInternal() started");

        long startTimestamp = System.currentTimeMillis();

        int effectiveDbVersion = getEffectiveDbVersion();
        LOG.debug("updateSecondaryDbInternal() effectiveDbVersion={}", effectiveDbVersion);

        String dbVersionParam = "_dbVer=" + effectiveDbVersion;
        String urlPath = WebService.getGetDatabaseUrlPart() + "/cached?" + dbVersionParam;

        try {
            Response response = WebService.call(urlPath, new HashMap<>());
            if (response != null) {
                ResponseBody body = response.body();
                MediaType contentType = body.contentType();
                LOG.debug("updateSecondaryDbInternal() response contentType={}", contentType);

                if (contentType != null && "application".equals(contentType.type())) {
                    LOG.trace("updateSecondaryDbInternal() saving response data to file");

                    File tempFile = File.createTempFile("sia", "database", Utils.getContext().getCacheDir());

                    int totalRead = 0;
                    try (InputStream in = body.byteStream();
                         OutputStream out = new FileOutputStream(tempFile)) {
                        byte[] buff = new byte[10240];

                        while (true) {
                            int read = in.read(buff);
                            if (read == -1) {
                                break;
                            }
                            out.write(buff, 0, read);
                            totalRead += read;
                        }
                    }

                    LOG.trace("updateSecondaryDbInternal() finished saving response data to file; totalRead={}", totalRead);

                    if (totalRead > 0) {
                        try (FileInputStream fis = new FileInputStream(tempFile);
                             BufferedInputStream bis = new BufferedInputStream(new GZIPInputStream(fis))) {
                            LOG.trace("updateSecondaryDbInternal() loading slice from received data");
                            CommunityDatabaseDataSlice slice = new CommunityDatabaseDataSlice();
                            if (slice.loadFromStream(bis)) {
                                createSecondaryDbDirectory();
                                LOG.trace("updateSecondaryDbInternal() distributing slice");
                                updateSecondaryWithSlice(slice);
                            }
                        }
                    }
                    LOG.trace("updateSecondaryDbInternal() finished processing slice");

                    if (!tempFile.delete()) {
                        LOG.warn("updateSecondaryDbInternal() failed to delete tempFile {}", tempFile);
                    }

                    LOG.debug("updateSecondaryDbInternal() updated performed successfully in {} ms",
                            System.currentTimeMillis() - startTimestamp);

                    return UpdateResult.UPDATED;
                } else {
                    String responseString = body.string();
                    LOG.debug("updateSecondaryDbInternal() responseString={}, elapsed time: {} ms",
                            responseString, System.currentTimeMillis() - startTimestamp);

                    responseString = responseString.replaceAll("\n", "");
                    switch (responseString) {
                        case "OAP":
                            LOG.trace("updateSecondaryDbInternal() server reported outdated app");
                            // outdated app
                            return UpdateResult.OUTDATED_APP;

                        case "NC":
                            LOG.trace("updateSecondaryDbInternal() server reported no updates");
                            // "No checkAndUpdate available" - probably "up to date"
                            return UpdateResult.NO_UPDATES;

                        case "OOD":
                            LOG.trace("updateSecondaryDbInternal() server suggests to reset secondary DB");
                            // remove secondary DB and retry
                            return UpdateResult.BAD_SECONDARY;
                    }
                }
            } else {
                LOG.warn("updateSecondaryDbInternal() response is null");
            }
        } catch (IOException e) {
            LOG.error("updateSecondaryDbInternal() IOE", e);
        } catch (Exception e) {
            LOG.error("updateSecondaryDbInternal()", e);
        }
        return UpdateResult.UNKNOWN_ERROR;
    }

    private boolean updateSecondaryWithSlice(CommunityDatabaseDataSlice dataSlice) {
        LOG.debug("updateSecondaryWithSlice() started");
        LOG.trace("updateSecondaryWithSlice() dataSlice={}", dataSlice);

        long startTimestamp = System.currentTimeMillis();
        try {
            SparseArray<List<Integer>> shortSliceIdToIndexMap = new SparseArray<>();
            SparseArray<List<Integer>> shortSliceIdToIndexToDeleteMap = new SparseArray<>();
            dataSlice.fillIndexMaps(shortSliceIdToIndexMap, shortSliceIdToIndexToDeleteMap);

            ArrayList<Integer> updatedIndexes = new ArrayList<>();
            for (int sliceId = 0; sliceId <= 99; sliceId++) {
                String filePath = getSecondarySliceFilePath(sliceId);

                CommunityDatabaseDataSlice newSlice = new CommunityDatabaseDataSlice();
                if (newSlice.partialClone(dataSlice, sliceId, shortSliceIdToIndexMap, shortSliceIdToIndexToDeleteMap)) {
                    CommunityDatabaseDataSlice sliceFromExistingFile = new CommunityDatabaseDataSlice();
                    if (getCachedSecondarySliceFilePath(sliceId) != null) {
                        loadSlice(sliceFromExistingFile, filePath, false);
                    }

                    try (BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(getDataDirPath() + filePath + ".update", false))) {
                        sliceFromExistingFile.writeMerged(newSlice, stream);
                    }

                    updatedIndexes.add(sliceId);

                    LOG.debug("updateSecondaryWithSlice() added {} items to sliceId={}", newSlice.getNumberOfItems(), sliceId);
                }
            }

            LOG.debug("updateSecondaryWithSlice() update files created, renaming files");

            for (int sliceId : updatedIndexes) {
                String filePath = getDataDirPath() + getSecondarySliceFilePath(sliceId);

                File updatedFile = new File(filePath + ".update");
                File oldFile = new File(filePath);
                if (oldFile.exists() && !oldFile.delete()) {
                    throw new IllegalStateException("Can't delete " + filePath);
                }
                if (!updatedFile.renameTo(oldFile)) {
                    throw new IllegalStateException("Can't replace slice " + updatedFile);
                }
            }

            Utils.getSettings().setSecondaryDbVersion(dataSlice.getDbVersion());
            secondarySliceCache.clear();
            existingSecondarySliceFiles.clear();

            LOG.debug("updateSecondaryWithSlice() finished in {} ms", System.currentTimeMillis() - startTimestamp);
            return true;
        } catch (Exception e) {
            LOG.error("updateSecondaryWithSlice()", e);
            return false;
        }
    }

}
