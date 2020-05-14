package dummydomain.yetanothercallblocker.sia.model.database;

import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dummydomain.yetanothercallblocker.sia.Storage;

public abstract class AbstractDatabase<T extends AbstractDatabaseDataSlice<V>, V> {

    public enum Source {
        INTERNAL, EXTERNAL, ANY
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabase.class);

    protected final Storage storage;
    protected final Source source;
    protected final String pathPrefix;

    protected boolean useInternal;

    protected int baseDatabaseVersion;
    protected int numberOfItems;

    protected DatabaseDataSliceNode dbRootNode;

    protected SparseArray<T> sliceCache = new SparseArray<>(); // TODO: cache cleaning

    protected boolean loaded;
    protected boolean failedToLoad;

    public AbstractDatabase(Storage storage, Source source, String pathPrefix) {
        this.storage = storage;
        this.source = source;
        this.pathPrefix = pathPrefix;
    }

    public boolean isOperational() {
        checkLoaded();
        return loaded;
    }

    public int getBaseDbVersion() {
        checkLoaded();
        return baseDatabaseVersion;
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

        if ((source == Source.ANY || source == Source.INTERNAL) && load(true)) {
            useInternal = true;
            loaded = true;
            return true;
        } else if ((source == Source.ANY || source == Source.EXTERNAL) && load(false)) {
            useInternal = false;
            loaded = true;
            return true;
        } else {
            LOG.warn("load() failed to load DB");
            failedToLoad = true;
            return false;
        }
    }

    protected boolean load(boolean useInternal) {
        if (loadInfoData(useInternal) && loadSliceListData(useInternal)) {
            LOG.info("load() loaded DB useInternal={}, baseDatabaseVersion={}, numberOfItems={}",
                    useInternal, this.baseDatabaseVersion, this.numberOfItems);
            return true;
        }
        return false;
    }

    protected String getPathPrefix() {
        return pathPrefix;
    }

    protected abstract String getNamePrefix();

    protected boolean loadInfoData(boolean useInternal) {
        LOG.debug("loadInfoData() started; useInternal: {}", useInternal);

        String fileName = getPathPrefix() + getNamePrefix() + "info.dat";

        try (InputStream is = storage.openFile(fileName, useInternal);
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

            loadInfoDataAfterLoadedHook(useInternal);

            return true;
        } catch (FileNotFoundException e) {
            LOG.debug("loadInfoData() the info-file wasn't found");
        } catch (Exception e) {
            LOG.error("loadInfoData() error during info file loading", e);
        }

        return false;
    }

    protected void loadInfoDataAfterLoadedHook(boolean useInternal) {}

    protected boolean loadSliceListData(boolean useInternal) {
        LOG.debug("loadSliceListData() started");

        String fileName = getPathPrefix() + getNamePrefix() + "list.dat";
        try (InputStream is = storage.openFile(fileName, useInternal);
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
        loadSlice(slice, fileName, useInternal);
    }

    protected void loadSlice(T slice, String fileName, boolean useInternal) {
        LOG.debug("loadSlice() started with fileName={}, useInternal={}", fileName, useInternal);

        try (InputStream is = storage.openFile(fileName, useInternal);
             BufferedInputStream stream = new BufferedInputStream(is)) {

            slice.loadFromStream(stream);
        } catch (Exception e) {
            LOG.error("loadSlice()", e);
        }
        LOG.trace("loadSlice() finished");
    }

}
