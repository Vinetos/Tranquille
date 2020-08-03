package dummydomain.yetanothercallblocker.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import dummydomain.yetanothercallblocker.PermissionHelper;
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
import dummydomain.yetanothercallblocker.sia.network.OkHttpClientFactory;
import dummydomain.yetanothercallblocker.sia.network.WebService;
import dummydomain.yetanothercallblocker.sia.utils.Utils;
import dummydomain.yetanothercallblocker.utils.DeferredInit;
import okhttp3.OkHttpClient;

import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PATH_PREFIX;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PROPERTIES;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_SECONDARY_PATH_PREFIX;

public class Config {

    private static class WSParameterProvider extends WebService.DefaultWSParameterProvider {
        dummydomain.yetanothercallblocker.Settings settings;
        SiaMetadata siaMetadata;
        CommunityDatabase communityDatabase;

        volatile String appId;
        volatile long appIdTimestamp;

        void setSettings(dummydomain.yetanothercallblocker.Settings settings) {
            this.settings = settings;
        }

        void setSiaMetadata(SiaMetadata siaMetadata) {
            this.siaMetadata = siaMetadata;
        }

        void setCommunityDatabase(CommunityDatabase communityDatabase) {
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

        WSParameterProvider wsParameterProvider = new WSParameterProvider();
        wsParameterProvider.setSettings(settings);

        WebService webService = new WebService(wsParameterProvider, okHttpClientFactory);
        DatabaseSingleton.setWebService(webService);

        DatabaseSingleton.setDbManager(new DbManager(storage, SIA_PATH_PREFIX,
                new DbDownloader(okHttpClientFactory)));

        CommunityDatabase communityDatabase = new CommunityDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX,
                SIA_SECONDARY_PATH_PREFIX, siaSettings, webService);

        wsParameterProvider.setCommunityDatabase(communityDatabase);
        DatabaseSingleton.setCommunityDatabase(communityDatabase);

        SiaMetadata siaMetadata = new SiaMetadata(storage, SIA_PATH_PREFIX,
                communityDatabase::isUsingInternal);

        wsParameterProvider.setSiaMetadata(siaMetadata);
        DatabaseSingleton.setSiaMetadata(siaMetadata);

        FeaturedDatabase featuredDatabase = new FeaturedDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX);
        DatabaseSingleton.setFeaturedDatabase(featuredDatabase);

        DatabaseSingleton.setCommunityReviewsLoader(new CommunityReviewsLoader(webService));

        YacbDaoSessionFactory daoSessionFactory = new YacbDaoSessionFactory(context, "YACB");

        BlacklistDao blacklistDao = new BlacklistDao(daoSessionFactory::getDaoSession);
        DatabaseSingleton.setBlacklistDao(blacklistDao);

        BlacklistService blacklistService = new BlacklistService(
                settings::setBlacklistIsNotEmpty, blacklistDao);
        DatabaseSingleton.setBlacklistService(blacklistService);

        ContactsProvider contactsProvider = number -> {
            if (settings.getUseContacts() && PermissionHelper.hasContactsPermission(context)) {
                String contactName = ContactsHelper.getContactName(context, number);
                if (!TextUtils.isEmpty(contactName)) return new ContactItem(contactName);
            }
            return null;
        };

        NumberInfoService numberInfoService = new NumberInfoService(
                settings, NumberUtils::isHiddenNumber,
                communityDatabase, featuredDatabase, contactsProvider, blacklistService);
        DatabaseSingleton.setNumberInfoService(numberInfoService);
    }

}
