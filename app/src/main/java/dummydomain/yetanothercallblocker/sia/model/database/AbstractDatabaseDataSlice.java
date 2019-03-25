package dummydomain.yetanothercallblocker.sia.model.database;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import dummydomain.yetanothercallblocker.sia.utils.LittleEndianDataInputStream;

public abstract class AbstractDatabaseDataSlice<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabaseDataSlice.class);

    protected int dbVersion;

    protected int numberOfItems;

    protected long[] numbers;

    protected long lastAccessTimestamp;

    public int getDbVersion() {
        return this.dbVersion;
    }

    public int getNumberOfItems() {
        return this.numberOfItems;
    }

    public long getLastAccessTimestamp() {
        return this.lastAccessTimestamp;
    }

    protected int indexOf(long number) {
        if (numberOfItems > 0) {
            int low = 0;
            int high = numberOfItems - 1;

            while (low <= high) {
                int mid = (high + low) / 2;
                long num = numbers[mid];

                if (num < number) {
                    low = mid + 1;
                } else if (num > number) {
                    high = mid - 1;
                } else {
                    return mid;
                }
            }
        }

        return -1;
    }

    public T getDbItemByNumber(long number) {
//        LOG.debug("getDbItemByNumber({}) started", number);

        lastAccessTimestamp = System.currentTimeMillis();

        int index = indexOf(number);
//        LOG.trace("getDbItemByNumber() index={}", index);
        if (index < 0) return null;

        return getDbItemByNumberInternal(number, index);
    }

    protected abstract T getDbItemByNumberInternal(long number, int index);

    protected void loadFromStreamCheckHeader(String header) {}

    protected void loadFromStreamReadPostHeaderData(LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamReadPostVersionData(LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamInitFields() {}

    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamLoadExtras(LittleEndianDataInputStream stream) throws IOException {
        int numberOfExtras = stream.readInt();
        if (numberOfExtras != 0) {
            throw new IllegalStateException("Number of extras is not 0: " + numberOfExtras);
        }
    }

    public boolean loadFromStream(BufferedInputStream inputStream) {
        LOG.debug("loadFromStream() started");

        try {
            long currentTimeMillis = System.currentTimeMillis();

            this.dbVersion = 0;

            LittleEndianDataInputStream stream = new LittleEndianDataInputStream(inputStream);

            LOG.trace("loadFromStream() reading header");
            String headerString = stream.readUtf8StringChars(4);
            loadFromStreamCheckHeader(headerString);

            LOG.trace("loadFromStream() reading post header data");
            loadFromStreamReadPostHeaderData(stream);

            LOG.trace("loadFromStream() reading DB version");
            this.dbVersion = stream.readInt();
            LOG.trace("loadFromStream() DB version is {}", dbVersion);

            LOG.trace("loadFromStream() reading post version data");
            loadFromStreamReadPostVersionData(stream);

            LOG.trace("loadFromStream() reading number of items");
            this.numberOfItems = stream.readInt();
            LOG.trace("loadFromStream() number of items is {}", numberOfItems);

            this.numbers = new long[this.numberOfItems];

            loadFromStreamInitFields();

            LOG.trace("loadFromStream() reading fields");
            for (int i = 0; i < this.numberOfItems; i++) {
                this.numbers[i] = stream.readLong();
                loadFromStreamLoadFields(i, stream);
            }
            LOG.trace("loadFromStream() finished reading fields");

            LOG.trace("loadFromStream() reading CP");
            String dividerString = stream.readUtf8StringChars(2);
            if (!"CP".equalsIgnoreCase(dividerString)) {
                throw new IllegalStateException("CP not found. Found instead: " + dividerString);
            }

            LOG.trace("loadFromStream() reading extras");
            loadFromStreamLoadExtras(stream);

            LOG.trace("loadFromStream() reading endmark");
            String endmarkString = stream.readUtf8StringChars(6);
            if (!"MTZEND".equalsIgnoreCase(endmarkString)) {
                throw new IllegalStateException("Endmark not found. Found instead: " + endmarkString);
            }

            LOG.debug("loadFromStream() loaded slice with {} items in {} ms",
                    numberOfItems, System.currentTimeMillis() - currentTimeMillis);
        } catch (Exception e) {
            LOG.error("loadFromStream()", e);
            return false;
        }
        return true;
    }

    protected String getAssetPathPrefix() {
        return "sia/";
    }

    protected abstract String getAssetNamePrefix();

    public void loadFromAsset(Context context, int sliceId) {
        LOG.debug("loadFromAsset() started with sliceId={}", sliceId);

        String assetName = getAssetPathPrefix() + getAssetNamePrefix() + String.valueOf(sliceId) + ".dat";
        LOG.trace("loadFromAsset() assetName={}", assetName);

        try (InputStream is = context.getAssets().open(assetName);
             BufferedInputStream stream = new BufferedInputStream(is)) {

            loadFromStream(stream);
        } catch (Exception e) {
            LOG.error("loadFromAsset()", e);
        }
        LOG.trace("loadFromAsset() finished");
    }

    public void loadFromFile(String fileName) {
        LOG.debug("loadFromFile({}) started", fileName);

        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream stream = new BufferedInputStream(fis)) {

            loadFromStream(stream);
        } catch (Exception e) {
            LOG.error("loadFromFile()", e);
        }
        LOG.trace("loadFromFile() finished");
    }

    @Override
    public String toString() {
        return "AbstractDatabaseDataSlice{" +
                "dbVersion=" + dbVersion +
                ", numberOfItems=" + numberOfItems +
                ", lastAccessTimestamp=" + lastAccessTimestamp +
                '}';
    }

}
