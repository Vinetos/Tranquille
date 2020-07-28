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
import dummydomain.yetanothercallblocker.sia.network.WebService;

public class DatabaseSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSingleton.class);

    private static WebService webService;

    private static DbManager dbManager;

    private static SiaMetadata siaMetadata;

    private static HiddenNumberDetector hiddenNumberDetector;

    private static CommunityDatabase communityDatabase;

    private static FeaturedDatabase featuredDatabase;

    private static CommunityReviewsLoader communityReviewsLoader;

    private static ContactsProvider contactsProvider;

    private static BlockingDecisionMaker blockingDecisionMaker;

    static void setWebService(WebService webService) {
        DatabaseSingleton.webService = webService;
    }

    static void setDbManager(DbManager dbManager) {
        DatabaseSingleton.dbManager = dbManager;
    }

    static void setSiaMetadata(SiaMetadata siaMetadata) {
        DatabaseSingleton.siaMetadata = siaMetadata;
    }

    static void setHiddenNumberDetector(HiddenNumberDetector hiddenNumberDetector) {
        DatabaseSingleton.hiddenNumberDetector = hiddenNumberDetector;
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

    static void setBlockingDecisionMaker(BlockingDecisionMaker blockingDecisionMaker) {
        DatabaseSingleton.blockingDecisionMaker = blockingDecisionMaker;
    }

    public static WebService getWebService() {
        return webService;
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

        NumberInfo numberInfo = new NumberInfo();
        numberInfo.number = number;

        if (hiddenNumberDetector != null) {
            numberInfo.isHiddenNumber = hiddenNumberDetector.isHiddenNumber(number);
        }
        LOG.trace("getNumberInfo() isHiddenNumber={}", numberInfo.isHiddenNumber);

        if (numberInfo.isHiddenNumber || TextUtils.isEmpty(numberInfo.number)) {
            numberInfo.noNumber = true;
        }
        LOG.trace("getNumberInfo() noNumber={}", numberInfo.noNumber);

        if (numberInfo.noNumber) {
            LOG.debug("getNumberInfo() finished");
            return numberInfo;
        }

        numberInfo.communityDatabaseItem = DatabaseSingleton.getCommunityDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() communityItem={}", numberInfo.communityDatabaseItem);

        numberInfo.featuredDatabaseItem = DatabaseSingleton.getFeaturedDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() featuredItem={}", numberInfo.featuredDatabaseItem);

        numberInfo.contactItem = DatabaseSingleton.contactsProvider.get(number);
        LOG.trace("getNumberInfo() contactItem={}", numberInfo.contactItem);

        ContactItem contactItem = numberInfo.contactItem;
        FeaturedDatabaseItem featuredItem = numberInfo.featuredDatabaseItem;
        if (contactItem != null && !TextUtils.isEmpty(contactItem.displayName)) {
            numberInfo.name = contactItem.displayName;
        } else if (featuredItem != null && !TextUtils.isEmpty(featuredItem.getName())) {
            numberInfo.name = featuredItem.getName();
        }
        LOG.trace("getNumberInfo() name={}", numberInfo.name);

        CommunityDatabaseItem communityItem = numberInfo.communityDatabaseItem;
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

        LOG.debug("getNumberInfo() finished");
        return numberInfo;
    }

    public static boolean shouldBlock(NumberInfo numberInfo) {
        if (blockingDecisionMaker != null) {
            return blockingDecisionMaker.shouldBlock(numberInfo);
        }
        return false;
    }

    public interface HiddenNumberDetector {
        boolean isHiddenNumber(String number);
    }

    public interface BlockingDecisionMaker {
        boolean shouldBlock(NumberInfo numberInfo);
    }

}
