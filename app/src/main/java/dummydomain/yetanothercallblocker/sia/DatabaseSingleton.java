package dummydomain.yetanothercallblocker.sia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

import static dummydomain.yetanothercallblocker.sia.SiaConstants.*;

public class DatabaseSingleton {

    private static final CommunityDatabase COMMUNITY_DATABASE = new CommunityDatabase(SIA_PATH_PREFIX, SIA_SECONDARY_PATH_PREFIX);
    private static final FeaturedDatabase FEATURED_DATABASE = new FeaturedDatabase(SIA_PATH_PREFIX);

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSingleton.class);

    public static CommunityDatabase getCommunityDatabase() {
        return COMMUNITY_DATABASE;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return FEATURED_DATABASE;
    }

    public static NumberInfo getNumberInfo(String number) {
        LOG.debug("getNumberInfo({}) started", number);
        // TODO: check number format

        CommunityDatabaseItem communityItem = DatabaseSingleton.getCommunityDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() communityItem={}", communityItem);

        FeaturedDatabaseItem featuredItem = DatabaseSingleton.getFeaturedDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() featuredItem={}", featuredItem);

        NumberInfo numberInfo = new NumberInfo();
        numberInfo.number = number;
        if (featuredItem != null) numberInfo.name = featuredItem.getName();

        if (communityItem != null && communityItem.hasRatings()) {
            if (communityItem.getNegativeRatingsCount() > communityItem.getPositiveRatingsCount()
                    + communityItem.getNeutralRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.NEGATIVE;
            } else if (communityItem.getPositiveRatingsCount() > communityItem.getNeutralRatingsCount()
                    + communityItem.getNegativeRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.POSITIVE;
            } else if (communityItem.getNeutralRatingsCount() > communityItem.getPositiveRatingsCount()
                    + communityItem.getNegativeRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.NEUTRAL;
            }
        }
        numberInfo.communityDatabaseItem = communityItem;

        LOG.trace("getNumberInfo() rating={}", numberInfo.rating);

        return numberInfo;
    }

}
