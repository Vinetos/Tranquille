package dummydomain.yetanothercallblocker.data;

import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class DatabaseSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSingleton.class);

    private static DbManager dbManager;

    private static SiaMetadata siaMetadata;

    private static CommunityDatabase communityDatabase;

    private static FeaturedDatabase featuredDatabase;

    private static CommunityReviewsLoader communityReviewsLoader;

    private static ContactsProvider contactsProvider;

    static void setDbManager(DbManager dbManager) {
        DatabaseSingleton.dbManager = dbManager;
    }

    static void setSiaMetadata(SiaMetadata siaMetadata) {
        DatabaseSingleton.siaMetadata = siaMetadata;
    }

    static void setCommunityDatabase(CommunityDatabase communityDatabase) {
        DatabaseSingleton.communityDatabase = communityDatabase;
    }

    static void setFeaturedDatabase(FeaturedDatabase featuredDatabase) {
        DatabaseSingleton.featuredDatabase = featuredDatabase;
    }

    static void setCommunityReviewsLoader(CommunityReviewsLoader communityReviewsLoader) {
        DatabaseSingleton.communityReviewsLoader = communityReviewsLoader;
    }

    static void setContactsProvider(ContactsProvider contactsProvider) {
        DatabaseSingleton.contactsProvider = contactsProvider;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static SiaMetadata getSiaMetadata() {
        return siaMetadata;
    }

    public static CommunityDatabase getCommunityDatabase() {
        return communityDatabase;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return featuredDatabase;
    }

    public static CommunityReviewsLoader getCommunityReviewsLoader() {
        return communityReviewsLoader;
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

        ContactItem contactItem = DatabaseSingleton.contactsProvider.get(number);
        LOG.trace("getNumberInfo() contactItem={}", contactItem);

        NumberInfo numberInfo = new NumberInfo();
        numberInfo.number = number;

        numberInfo.communityDatabaseItem = communityItem;
        numberInfo.featuredDatabaseItem = featuredItem;
        numberInfo.contactItem = contactItem;

        if (contactItem != null && !TextUtils.isEmpty(contactItem.displayName)) {
            numberInfo.name = contactItem.displayName;
        } else if (featuredItem != null && !TextUtils.isEmpty(featuredItem.getName())) {
            numberInfo.name = featuredItem.getName();
        }
        LOG.trace("getNumberInfo() name={}", numberInfo.name);

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
        LOG.trace("getNumberInfo() rating={}", numberInfo.rating);

        return numberInfo;
    }

}
