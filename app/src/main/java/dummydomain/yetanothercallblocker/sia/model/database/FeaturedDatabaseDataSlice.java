package dummydomain.yetanothercallblocker.sia.model.database;

import java.io.IOException;

import dummydomain.yetanothercallblocker.sia.utils.LittleEndianDataInputStream;

public class FeaturedDatabaseDataSlice extends AbstractDatabaseDataSlice<FeaturedDatabaseItem> {

    private String[] names;

    @Override
    protected FeaturedDatabaseItem getDbItemByNumberInternal(long number, int index) {
        return new FeaturedDatabaseItem(number, names[index]);
    }

    @Override
    protected void loadFromStreamCheckHeader(String header) {
        if (!"YABX".equalsIgnoreCase(header) && !"MTZX".equalsIgnoreCase(header)) {
            throw new IllegalStateException("Invalid header. Actual value: " + header);
        }
    }

    @Override
    protected void loadFromStreamInitFields() {
        names = new String[numberOfItems];
    }

    @Override
    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {
        int nameLength = stream.readInt();
        names[index] = stream.readUtf8StringChars(nameLength);
    }

}
