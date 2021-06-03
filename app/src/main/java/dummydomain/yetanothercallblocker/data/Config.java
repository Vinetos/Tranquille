package dummydomain.yetanothercallblocker.data;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import dummydomain.yetanothercallblocker.NotificationService;
import dummydomain.yetanothercallblocker.PhoneStateHandler;
import dummydomain.yetanothercallblocker.data.db.BlacklistDao;
import dummydomain.yetanothercallblocker.data.db.YacbDaoSessionFactory;
import dummydomain.yetanothercallblocker.sia.Settings;
import dummydomain.yetanothercallblocker.sia.SettingsImpl;
import dummydomain.yetanothercallblocker.sia.Storage;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata;
import dummydomain.yetanothercallblocker.sia.model.database.AbstractDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.network.DbDownloader;
import dummydomain.yetanothercallblocker.sia.network.DbUpdateRequester;
import dummydomain.yetanothercallblocker.sia.network.OkHttpClientFactory;
import dummydomain.yetanothercallblocker.sia.network.WebService;
import dummydomain.yetanothercallblocker.sia.utils.Utils;
import dummydomain.yetanothercallblocker.utils.DbFilteringUtils;
import dummydomain.yetanothercallblocker.utils.DeferredInit;
import dummydomain.yetanothercallblocker.utils.SystemUtils;
import okhttp3.OkHttpClient;

import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PATH_PREFIX;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PROPERTIES;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_SECONDARY_PATH_PREFIX;

public class Config {

    private static class WSParameterProvider extends WebService.DefaultWSParameterProvider {
        final dummydomain.yetanothercallblocker.Settings settings;
        final SiaMetadata siaMetadata;
        final CommunityDatabase communityDatabase;

        volatile String appId;
        volatile long appIdTimestamp;

        WSParameterProvider(dummydomain.yetanothercallblocker.Settings settings,
                            SiaMetadata siaMetadata, CommunityDatabase communityDatabase) {
            this.settings = settings;
            this.siaMetadata = siaMetadata;
            this.communityDatabase = communityDatabase;
        }

        @Override
        public String getAppId() {
            String appId = this.appId;
            if (appId != null && System.nanoTime() >
                    appIdTimestamp + TimeUnit.MINUTES.toNanos(5)) {
                appId = null;
            }

            if (appId == null) {
                this.appId = appId = Utils.generateAppId();
                appIdTimestamp = System.nanoTime();
            }

            return appId;
        }

        @Override
        public int getAppVersion() {
            return siaMetadata.getSiaAppVersion();
        }

        @Override
        public String getOkHttpVersion() {
            return siaMetadata.getSiaOkHttpVersion();
        }

        @Override
        public int getDbVersion() {
            return communityDatabase.getEffectiveDbVersion();
        }

        @Override
        public SiaMetadata.Country getCountry() {
            return siaMetadata.getCountry(settings.getCountryCode());
        }
    }

    public static void init(Context context, dummydomain.yetanothercallblocker.Settings settings) {
        Storage storage = new AndroidStorage(context);
        Settings siaSettings
                = new SettingsImpl(new AndroidProperties(context, SIA_PROPERTIES));

        OkHttpClientFactory okHttpClientFactory = () -> {
            DeferredInit.initNetwork();
            return new OkHttpClient();
        };

        CommunityDatabase communityDatabase = new CommunityDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX,
                SIA_SECONDARY_PATH_PREFIX, siaSettings);
        YacbHolder.setCommunityDatabase(communityDatabase);

        SiaMetadata siaMetadata = new SiaMetadata(storage, SIA_PATH_PREFIX,
                communityDatabase::isUsingInternal);
        YacbHolder.setSiaMetadata(siaMetadata);

        FeaturedDatabase featuredDatabase = new FeaturedDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX);
        YacbHolder.setFeaturedDatabase(featuredDatabase);

        WSParameterProvider wsParameterProvider = new WSParameterProvider(
                settings, siaMetadata, communityDatabase);

        WebService webService = new WebService(wsParameterProvider, okHttpClientFactory);
        YacbHolder.setWebService(webService);

        YacbHolder.setDbManager(new DbManager(storage, SIA_PATH_PREFIX,
                new DbDownloader(okHttpClientFactory), new DbUpdateRequester(webService),
                communityDatabase));

        YacbHolder.getDbManager().setNumberFilter(DbFilteringUtils.getNumberFilter(settings));

        YacbHolder.setCommunityReviewsLoader(new CommunityReviewsLoader(webService));

        YacbDaoSessionFactory daoSessionFactory = new YacbDaoSessionFactory(context, "YACB");

        BlacklistDao blacklistDao = new BlacklistDao(daoSessionFactory::getDaoSession);
        YacbHolder.setBlacklistDao(blacklistDao);

        BlacklistService blacklistService = new BlacklistService(
                settings::setBlacklistIsNotEmpty, blacklistDao);
        YacbHolder.setBlacklistService(blacklistService);

        ContactsProvider contactsProvider = new ContactsProvider() {
            @Override
            public ContactItem get(String number) {
                return settings.getUseContacts() ? ContactsHelper.getContact(context, number) : null;
            }

            @Override
            public boolean isInLimitedMode() {
                return !SystemUtils.isUserUnlocked(context);
            }
        };

        NumberInfoService numberInfoService = new NumberInfoService(
                settings, NumberUtils::isHiddenNumber, NumberUtils::normalizeNumber,
                communityDatabase, featuredDatabase, contactsProvider, blacklistService);
        YacbHolder.setNumberInfoService(numberInfoService);

        NotificationService notificationService = new NotificationService(context);
        YacbHolder.setNotificationService(notificationService);

        YacbHolder.setPhoneStateHandler(
                new PhoneStateHandler(context, settings, numberInfoService, notificationService));
    }

}
