package dummydomain.yetanothercallblocker.data;

import dummydomain.yetanothercallblocker.data.db.BlacklistDao;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.network.WebService;

public class DatabaseSingleton {

    private static WebService webService;
    private static DbManager dbManager;
    private static SiaMetadata siaMetadata;
    private static CommunityDatabase communityDatabase;
    private static FeaturedDatabase featuredDatabase;
    private static CommunityReviewsLoader communityReviewsLoader;

    private static BlacklistDao blacklistDao;
    private static BlacklistService blacklistService;

    private static NumberInfoService numberInfoService;

    static void setWebService(WebService webService) {
        DatabaseSingleton.webService = webService;
    }

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

    static void setBlacklistDao(BlacklistDao blacklistDao) {
        DatabaseSingleton.blacklistDao = blacklistDao;
    }

    static void setBlacklistService(BlacklistService blacklistService) {
        DatabaseSingleton.blacklistService = blacklistService;
    }

    static void setNumberInfoService(NumberInfoService numberInfoService) {
        DatabaseSingleton.numberInfoService = numberInfoService;
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

    public static BlacklistDao getBlacklistDao() {
        return blacklistDao;
    }

    public static BlacklistService getBlacklistService() {
        return blacklistService;
    }

    public static NumberInfoService getNumberInfoService() {
        return numberInfoService;
    }

    public static NumberInfo getNumberInfo(String number) {
        return numberInfoService.getNumberInfo(number, true);
    }

}
