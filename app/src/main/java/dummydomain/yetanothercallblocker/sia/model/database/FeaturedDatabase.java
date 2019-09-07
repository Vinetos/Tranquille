package dummydomain.yetanothercallblocker.sia.model.database;

public class FeaturedDatabase extends AbstractDatabase<FeaturedDatabaseDataSlice, FeaturedDatabaseItem> {

    public FeaturedDatabase(String pathPrefix) {
        super(pathPrefix);
    }

    @Override
    protected String getNamePrefix() {
        return "featured_slice_";
    }

    @Override
    protected FeaturedDatabaseItem getDbItemByNumberInternal(long number) {
        FeaturedDatabaseDataSlice slice = getDataSlice(number);
        return slice != null ? slice.getDbItemByNumber(number) : null;
    }

    @Override
    protected FeaturedDatabaseDataSlice createDbDataSlice() {
        return new FeaturedDatabaseDataSlice();
    }

}
