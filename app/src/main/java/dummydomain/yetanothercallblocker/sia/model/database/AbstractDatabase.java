package dummydomain.yetanothercallblocker.sia.model.database;

import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dummydomain.yetanothercallblocker.sia.utils.Utils;

public abstract class AbstractDatabase<T extends AbstractDatabaseDataSlice<V>, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabase.class);

    protected int baseDatabaseVersion;
    protected int numberOfItems;

    protected DatabaseDataSliceNode dbRootNode;

    protected SparseArray<T> sliceCache = new SparseArray<>(); // TODO: cache cleaning

    protected boolean loaded;
    protected boolean failedToLoad;

    protected void checkLoaded() {
        if (loaded) return;
        if (failedToLoad) throw new IllegalStateException("The DB has failed to load");

        LOG.debug("checkLoaded() loading DB");
        loaded = load();
        failedToLoad = !loaded;
    }

    protected boolean load() {
        LOG.debug("load() loading DB");

        if (loadInfoData() && loadSliceListData()) {
            LOG.info("load() loaded DB, baseDatabaseVersion={}, numberOfItems={}",
                    this.baseDatabaseVersion, this.numberOfItems);
            return true;
        } else {
            LOG.warn("load() failed to load DB");
            return false;
        }
    }

    protected String getAssetPathPrefix() {
        return "sia/";
    }

    protected abstract String getAssetNamePrefix();

    protected boolean loadInfoData() {
        LOG.debug("loadInfoData() started");

        String fileName = getAssetPathPrefix() + getAssetNamePrefix() + "info.dat";
        try (InputStream is = Utils.getContext().getAssets().open(fileName);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            if (!"MDI".equals(bufferedReader.readLine())) {
                LOG.debug("loadInfoData() incorrect header");
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

            loadInfoDataAfterLoadedHook();

            return true;
        } catch (FileNotFoundException e) {
            LOG.error("loadInfoData() the info-file is not found! HAVE YOU COPIED THE ASSETS? (see `Building` in README)", e);
        } catch (Exception e) {
            LOG.error("loadInfoData() error during info file loading", e);
        }

        return false;
    }

    protected void loadInfoDataAfterLoadedHook() {}

    protected boolean loadSliceListData() {
        LOG.debug("loadSliceListData() started");

        String fileName = getAssetPathPrefix() + getAssetNamePrefix() + "list.dat";
        try (InputStream is = Utils.getContext().getAssets().open(fileName);
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

        checkLoaded();

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
            slice.loadFromAsset(Utils.getContext(), sliceId);

            sliceCache.put(sliceId, slice);
        } else {
            LOG.trace("getDataSlice() found slice in cache");
        }
        return slice;
    }

}
