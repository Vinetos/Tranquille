package dummydomain.yetanothercallblocker.sia.model.database;

import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static dummydomain.yetanothercallblocker.sia.utils.FileUtils.openFile;

public abstract class AbstractDatabase<T extends AbstractDatabaseDataSlice<V>, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabase.class);

    protected final String pathPrefix;

    protected boolean useAssets;

    protected int baseDatabaseVersion;
    protected int numberOfItems;

    protected DatabaseDataSliceNode dbRootNode;

    protected SparseArray<T> sliceCache = new SparseArray<>(); // TODO: cache cleaning

    protected boolean loaded;
    protected boolean failedToLoad;

    public AbstractDatabase(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public boolean isOperational() {
        checkLoaded();
        return loaded;
    }

    public boolean reload() {
        return load();
    }

    protected void checkLoaded() {
        if (loaded || failedToLoad) return;

        LOG.debug("checkLoaded() loading DB");
        load();
    }

    protected void reset() {
        loaded = false;
        failedToLoad = false;

        baseDatabaseVersion = 0;
        numberOfItems = 0;

        dbRootNode = null;
        sliceCache.clear();
    }

    protected boolean load() {
        LOG.debug("load() loading DB");

        reset();

        if (load(true)) {
            useAssets = true;
            loaded = true;
            return true;
        } else if (load(false)) {
            useAssets = false;
            loaded = true;
            return true;
        } else {
            LOG.warn("load() failed to load DB");
            failedToLoad = true;
            return false;
        }
    }

    protected boolean load(boolean useAssets) {
        if (loadInfoData(useAssets) && loadSliceListData(useAssets)) {
            LOG.info("load() loaded DB useAssets={}, baseDatabaseVersion={}, numberOfItems={}",
                    useAssets, this.baseDatabaseVersion, this.numberOfItems);
            return true;
        }
        return false;
    }

    protected String getPathPrefix() {
        return pathPrefix;
    }

    protected abstract String getNamePrefix();

    protected boolean loadInfoData(boolean useAssets) {
        LOG.debug("loadInfoData() started; useAssets: {}", useAssets);

        String fileName = getPathPrefix() + getNamePrefix() + "info.dat";

        try (InputStream is = openFile(fileName, useAssets);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            String headerString = bufferedReader.readLine();
            if (!"YACBSIAI".equals(headerString) && !"MDI".equals(headerString)) {
                LOG.debug("loadInfoData() incorrect header: {}", headerString);
                return false;
            }

            // NB: SIA ignores Integer.parseInt() exceptions and falls back to 0

            String dbVersionString = bufferedReader.readLine();
            int dbVersion = Integer.parseInt(dbVersionString);

            String numberOfItemsString = bufferedReader.readLine();
            int numberOfItems = Integer.parseInt(numberOfItemsString);

            this.baseDatabaseVersion = dbVersion;
            this.numberOfItems = numberOfItems;

            LOG.debug("loadInfoData() loaded MDI, baseDatabaseVersion={}, numberOfItems={}",
                    this.baseDatabaseVersion, this.numberOfItems);

            loadInfoDataAfterLoadedHook(useAssets);

            return true;
        } catch (FileNotFoundException e) {
            LOG.debug("loadInfoData() the info-file wasn't found");
        } catch (Exception e) {
            LOG.error("loadInfoData() error during info file loading", e);
        }

        return false;
    }

    protected void loadInfoDataAfterLoadedHook(boolean useAssets) {}

    protected boolean loadSliceListData(boolean useAssets) {
        LOG.debug("loadSliceListData() started");

        String fileName = getPathPrefix() + getNamePrefix() + "list.dat";
        try (InputStream is = openFile(fileName, useAssets);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
            String nodes = bufferedReader.readLine();

            dbRootNode = new DatabaseDataSliceNode();
            dbRootNode.init(nodes, 0);

            LOG.info("loadSliceListData() loaded slice list data");
            return true;
        } catch (Exception e) {
            LOG.error("loadSliceListData() error", e);
        }

        return false;
    }

    protected abstract V getDbItemByNumberInternal(long number);

    public V getDbItemByNumber(String numberString) {
        LOG.info("getDbItemByNumber({}) started", numberString);

        if (!isOperational()) {
            LOG.warn("getDbItemByNumber() request on a non-operational DB; returning null");
            return null;
        }

        if (numberString == null || numberString.isEmpty()) return null;

        if (numberString.startsWith("+")) {
            numberString = numberString.substring(1);
        }

        long number;
        try {
            number = Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            LOG.warn("getDbItemByNumber() couldn't parse number: " + numberString, e);
            return null;
        }

        LOG.debug("getDbItemByNumber() calling internal method for {}", number);
        return getDbItemByNumberInternal(number);
    }

    protected abstract T createDbDataSlice();

    protected T getDataSlice(long number) {
        LOG.debug("getDataSlice({}) started", number);

        if (number <= 0) return null;

        int sliceId = dbRootNode.getSliceId(0, String.valueOf(number));
        LOG.trace("getDataSlice() sliceId={}", sliceId);
        if (sliceId <= 0) return null;

        T slice = sliceCache.get(sliceId);
        if (slice == null) {
            LOG.trace("getDataSlice() loading slice with sliceId={}", sliceId);

            slice = createDbDataSlice();
            loadSlice(slice, sliceId);

            sliceCache.put(sliceId, slice);
        } else {
            LOG.trace("getDataSlice() found slice in cache");
        }
        return slice;
    }

    protected void loadSlice(T slice, int sliceId) {
        LOG.debug("loadSlice() started with sliceId={}", sliceId);

        String fileName = getPathPrefix() + getNamePrefix() + sliceId + ".dat";
        loadSlice(slice, fileName, useAssets);
    }

    protected void loadSlice(T slice, String fileName, boolean useAssets) {
        LOG.debug("loadSlice() started with fileName={}, useAssets={}", fileName, useAssets);

        try (InputStream is = openFile(fileName, useAssets);
             BufferedInputStream stream = new BufferedInputStream(is)) {

            slice.loadFromStream(stream);
        } catch (Exception e) {
            LOG.error("loadSlice()", e);
        }
        LOG.trace("loadSlice() finished");
    }

}
