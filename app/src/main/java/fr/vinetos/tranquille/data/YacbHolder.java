package fr.vinetos.tranquille.data;

import android.annotation.SuppressLint;

import fr.vinetos.tranquille.NotificationService;
import fr.vinetos.tranquille.PhoneStateHandler;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.network.WebService;
import fr.vinetos.tranquille.data.datasource.DenylistDataSource;
import fr.vinetos.tranquille.domain.service.DenylistService;

public class YacbHolder {

    private static WebService webService;
    private static DbManager dbManager;
    private static SiaMetadata siaMetadata;
    private static CommunityDatabase communityDatabase;
    private static FeaturedDatabase featuredDatabase;
    private static CommunityReviewsLoader communityReviewsLoader;

    private static DenylistDataSource denylistDataSource;
    private static DenylistService denylistService;

    private static NumberInfoService numberInfoService;

    @SuppressLint("StaticFieldLeak")
    private static NotificationService notificationService;

    @SuppressLint("StaticFieldLeak")
    private static PhoneStateHandler phoneStateHandler;

    static void setWebService(WebService webService) {
        YacbHolder.webService = webService;
    }

    static void setDbManager(DbManager dbManager) {
        YacbHolder.dbManager = dbManager;
    }

    static void setSiaMetadata(SiaMetadata siaMetadata) {
        YacbHolder.siaMetadata = siaMetadata;
    }

    static void setCommunityDatabase(CommunityDatabase communityDatabase) {
        YacbHolder.communityDatabase = communityDatabase;
    }

    static void setFeaturedDatabase(FeaturedDatabase featuredDatabase) {
        YacbHolder.featuredDatabase = featuredDatabase;
    }

    static void setCommunityReviewsLoader(CommunityReviewsLoader communityReviewsLoader) {
        YacbHolder.communityReviewsLoader = communityReviewsLoader;
    }

    static void setDenylistDataSource(DenylistDataSource denylistDataSource) {
        YacbHolder.denylistDataSource = denylistDataSource;
    }

    static void setBlacklistService(DenylistService denylistService) {
        YacbHolder.denylistService = denylistService;
    }

    static void setNumberInfoService(NumberInfoService numberInfoService) {
        YacbHolder.numberInfoService = numberInfoService;
    }

    static void setNotificationService(NotificationService notificationService) {
        YacbHolder.notificationService = notificationService;
    }

    static void setPhoneStateHandler(PhoneStateHandler phoneStateHandler) {
        YacbHolder.phoneStateHandler = phoneStateHandler;
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

    public static DenylistDataSource getDenylistDataSource() {
        return denylistDataSource;
    }

    public static DenylistService getBlacklistService() {
        return denylistService;
    }

    public static NumberInfoService getNumberInfoService() {
        return numberInfoService;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

    public static PhoneStateHandler getPhoneStateHandler() {
        return phoneStateHandler;
    }

    public static NumberInfo getNumberInfo(String number, String countryCode) {
        return numberInfoService.getNumberInfo(number, countryCode, true);
    }

}
