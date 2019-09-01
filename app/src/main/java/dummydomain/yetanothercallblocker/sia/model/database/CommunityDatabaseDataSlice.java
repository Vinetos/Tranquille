package dummydomain.yetanothercallblocker.sia.model.database;

import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.sia.utils.LittleEndianDataInputStream;
import dummydomain.yetanothercallblocker.sia.utils.LittleEndianDataOutputStream;

public class CommunityDatabaseDataSlice extends AbstractDatabaseDataSlice<CommunityDatabaseItem> {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityDatabaseDataSlice.class);

    private byte[] positiveRatingsCounts;
    private byte[] negativeRatingsCounts;
    private byte[] neutralRatingsCounts;
    private byte[] unknownData;
    private byte[] categories;

    private long[] numbersToDelete;

    public void fillIndexMaps(SparseArray<List<Integer>> shortSliceIdToIndexMap,
                              SparseArray<List<Integer>> shortSliceIdToIndexToDeleteMap) {
        for (int i = 0; i <= 99; i++) {
            shortSliceIdToIndexMap.put(i, new ArrayList<>());
            shortSliceIdToIndexToDeleteMap.put(i, new ArrayList<>());
        }

        for (int i = 0; i < numbers.length; i++) {
            String numberString = String.valueOf(numbers[i]);
            if (numberString.length() > 1) {
                int sliceId = Integer.valueOf(numberString.substring(0, 2));
                shortSliceIdToIndexMap.get(sliceId).add(i);
            }
        }
        for (int i = 0; i < numbersToDelete.length; i++) {
            String numberString = String.valueOf(numbersToDelete[i]);
            if (numberString.length() > 1) {
                int sliceId = Integer.valueOf(numberString.substring(0, 2));
                shortSliceIdToIndexToDeleteMap.get(sliceId).add(i);
            }
        }
    }

    public boolean partialClone(CommunityDatabaseDataSlice source, int shortSliceId,
                                SparseArray<List<Integer>> shortSliceIdToIndexMap,
                                SparseArray<List<Integer>> shortSliceIdToIndexToDeleteMap) {
        List<Integer> numberIndexList = shortSliceIdToIndexMap.get(shortSliceId);
        this.numberOfItems = numberIndexList.size();
        this.dbVersion = source.dbVersion;
        this.numbers = new long[this.numberOfItems];
        this.positiveRatingsCounts = new byte[this.numberOfItems];
        this.negativeRatingsCounts = new byte[this.numberOfItems];
        this.neutralRatingsCounts = new byte[this.numberOfItems];
        this.unknownData = new byte[this.numberOfItems];
        this.categories = new byte[this.numberOfItems];
        for (int i = 0; i < this.numberOfItems; i++) {
            int index = numberIndexList.get(i);
            this.numbers[i] = source.numbers[index];
            this.positiveRatingsCounts[i] = source.positiveRatingsCounts[index];
            this.negativeRatingsCounts[i] = source.negativeRatingsCounts[index];
            this.neutralRatingsCounts[i] = source.neutralRatingsCounts[index];
            this.unknownData[i] = source.unknownData[index];
            this.categories[i] = source.categories[index];
        }

        List<Integer> numbersToDeleteIndexList = shortSliceIdToIndexToDeleteMap.get(shortSliceId);
        this.numbersToDelete = new long[numbersToDeleteIndexList.size()];
        for (int i = 0; i < numbersToDelete.length; i++) {
            this.numbersToDelete[i] = source.numbersToDelete[numbersToDeleteIndexList.get(i)];
        }

        return this.numberOfItems > 0 || numbersToDelete.length > 0;
    }

    @Override
    protected CommunityDatabaseItem getDbItemByNumberInternal(long number, int index) {
        CommunityDatabaseItem communityDatabaseItem = new CommunityDatabaseItem();
        communityDatabaseItem.setNumber(numbers[index]);
        communityDatabaseItem.setPositiveRatingsCount(positiveRatingsCounts[index] & 255);
        communityDatabaseItem.setNegativeRatingsCount(negativeRatingsCounts[index] & 255);
        communityDatabaseItem.setNeutralRatingsCount(neutralRatingsCounts[index] & 255);
        communityDatabaseItem.setUnknownData(unknownData[index] & 255);
        communityDatabaseItem.setCategory(categories[index] & 255);
        return communityDatabaseItem;
    }

    @Override
    protected void loadFromStreamCheckHeader(String header) {
        if (!"YABF".equalsIgnoreCase(header) && !"MTZF".equalsIgnoreCase(header)
                && !"MTZD".equalsIgnoreCase(header)) {
            throw new IllegalStateException("Invalid header. Actual value: " + header);
        }
    }

    @Override
    protected void loadFromStreamReadPostHeaderData(LittleEndianDataInputStream stream) throws IOException {
        byte b = stream.readByte(); // ignored
        LOG.trace("loadFromStreamReadPostHeaderData() b={}", b);
    }

    @Override
    protected void loadFromStreamReadPostVersionData(LittleEndianDataInputStream stream) throws IOException {
        String s = stream.readUtf8StringChars(2); // ignored
        int i = stream.readInt(); // ignored
        LOG.trace("loadFromStreamReadPostVersionData() s={}, i={}", s, i);
    }

    @Override
    protected void loadFromStreamInitFields() {
        positiveRatingsCounts = new byte[numberOfItems];
        negativeRatingsCounts = new byte[numberOfItems];
        neutralRatingsCounts = new byte[numberOfItems];
        unknownData = new byte[numberOfItems];
        categories = new byte[numberOfItems];
    }

    @Override
    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {
        positiveRatingsCounts[index] = stream.readByte();
        negativeRatingsCounts[index] = stream.readByte();
        neutralRatingsCounts[index] = stream.readByte();
        unknownData[index] = stream.readByte();
        categories[index] = stream.readByte();
    }

    @Override
    protected void loadFromStreamLoadExtras(LittleEndianDataInputStream stream) throws IOException {
        int numberOfItemsToDelete = stream.readInt();
        LOG.trace("loadFromStreamLoadExtras() numberOfItemsToDelete={}", numberOfItemsToDelete);

        numbersToDelete = new long[numberOfItemsToDelete];
        for (int i = 0; i < numberOfItemsToDelete; i++) {
            numbersToDelete[i] = stream.readLong();
        }
    }

    @Override
    protected String getAssetNamePrefix() {
        return "data_slice_";
    }

    public boolean writeMerged(CommunityDatabaseDataSlice newSlice, BufferedOutputStream outputStream) {
        LOG.debug("writeMerged() started with newSlice={}", newSlice);

        try {
            int realNumberOfItems = 0;
            int newNumberOfItems = this.numberOfItems;
            if (newSlice != null) {
                if (this.numberOfItems > 0) {
                    for (long n : newSlice.numbers) {
                        if (indexOf(n) < 0) {
                            newNumberOfItems++;
                        }
                    }
                    for (long n : newSlice.numbersToDelete) {
                        if (indexOf(n) < 0) {
                            newNumberOfItems++;
                        }
                    }
                } else {
                    newNumberOfItems = newSlice.numberOfItems + newSlice.numbersToDelete.length;
                }
            }

            LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(outputStream);
            stream.writeUtf8StringChars("YABF");
            stream.writeByte((byte) 1);
            stream.writeInt(newSlice != null ? newSlice.dbVersion : this.dbVersion);
            stream.writeUtf8StringChars("ww");
            stream.writeInt(0);
            stream.writeInt(newNumberOfItems);

            if (this.numberOfItems >= 0) {
                int sourceIndex = 0;
                int newIndex = 0;
                int newDeletedIndex = 0;
                do {
                    long sourceNumber = sourceIndex < this.numberOfItems ? this.numbers[sourceIndex] : Long.MAX_VALUE;
                    boolean replacesSourceNumber = false;
                    if (newSlice != null) {
                        while (true) {
                            long newNumber = (newIndex >= newSlice.numbers.length || newSlice.numbers[newIndex] > sourceNumber) ? 0 : newSlice.numbers[newIndex];
                            long newDeletedNumber = (newDeletedIndex >= newSlice.numbersToDelete.length || newSlice.numbersToDelete[newDeletedIndex] > sourceNumber) ? 0 : newSlice.numbersToDelete[newDeletedIndex];
                            if (newNumber <= 0 || (newDeletedNumber != 0 && newNumber >= newDeletedNumber)) {
                                if (newDeletedNumber <= 0) {
                                    break;
                                }
                                stream.writeLong(newSlice.numbersToDelete[newDeletedIndex]);
                                byte zero = (byte) 0;
                                stream.writeByte(zero);
                                stream.writeByte(zero);
                                stream.writeByte(zero);
                                stream.writeByte(zero);
                                stream.writeByte(zero);
                                replacesSourceNumber = newSlice.numbersToDelete[newDeletedIndex] == sourceNumber;
                                newDeletedIndex++;
                                realNumberOfItems++;
                            } else {
                                stream.writeLong(newSlice.numbers[newIndex]);
                                stream.writeByte(newSlice.positiveRatingsCounts[newIndex]);
                                stream.writeByte(newSlice.negativeRatingsCounts[newIndex]);
                                stream.writeByte(newSlice.neutralRatingsCounts[newIndex]);
                                stream.writeByte(newSlice.unknownData[newIndex]);
                                stream.writeByte(newSlice.categories[newIndex]);
                                replacesSourceNumber = newSlice.numbers[newIndex] == sourceNumber;
                                newIndex++;
                                realNumberOfItems++;
                            }
                        }
                    }
                    if (!replacesSourceNumber && sourceIndex < this.numberOfItems) {
                        stream.writeLong(this.numbers[sourceIndex]);
                        stream.writeByte(this.positiveRatingsCounts[sourceIndex]);
                        stream.writeByte(this.negativeRatingsCounts[sourceIndex]);
                        stream.writeByte(this.neutralRatingsCounts[sourceIndex]);
                        stream.writeByte(this.unknownData[sourceIndex]);
                        stream.writeByte(this.categories[sourceIndex]);
                        realNumberOfItems++;
                    }
                } while (sourceIndex++ != this.numberOfItems);
            }
            if (realNumberOfItems != newNumberOfItems) {
                LOG.error("writeMerged() realNumberOfItems={}, newNumberOfItems={}, dbVersion={}, newSlice.dbVersion={}",
                        realNumberOfItems, newNumberOfItems, dbVersion, newSlice != null ? newSlice.dbVersion : 0);
                throw new IllegalStateException("writeMerged results in invalid realNumberOfItems!");
            }
            stream.writeUtf8StringChars("CP");
            stream.writeInt(0);
            stream.writeUtf8StringChars("YABEND");
            return true;
        } catch (IOException e) {
            LOG.error("writeMerged()", e);
            return false;
        }
    }

}
