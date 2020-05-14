package dummydomain.yetanothercallblocker.data;

import android.content.Context;
import android.text.TextUtils;

import dummydomain.yetanothercallblocker.PermissionHelper;
import dummydomain.yetanothercallblocker.sia.Settings;
import dummydomain.yetanothercallblocker.sia.SettingsImpl;
import dummydomain.yetanothercallblocker.sia.Storage;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.database.AbstractDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.network.WebService;

import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PATH_PREFIX;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_PROPERTIES;
import static dummydomain.yetanothercallblocker.data.SiaConstants.SIA_SECONDARY_PATH_PREFIX;

public class Config {

    private static class WSParameterProvider extends WebService.DefaultWSParameterProvider {
        CommunityDatabase communityDatabase;

        void setCommunityDatabase(CommunityDatabase communityDatabase) {
            this.communityDatabase = communityDatabase;
        }

        @Override
        public String getAppId() {
            return "qQq0O9nCRNy_aVdPgU9WOA";
        }

        @Override
        public int getAppVersion() {
            return communityDatabase.getSiaAppVersion();
        }

        @Override
        public int getDbVersion() {
            return communityDatabase.getEffectiveDbVersion();
        }
    }

    public static void init(Context context, dummydomain.yetanothercallblocker.Settings settings) {
        Storage storage = new AndroidStorage(context);
        Settings siaSettings
                = new SettingsImpl(new AndroidProperties(context, SIA_PROPERTIES));

        WSParameterProvider wsParameterProvider = new WSParameterProvider();
        WebService webService = new WebService(wsParameterProvider);

        DatabaseSingleton.setDbManager(new DbManager(storage, SIA_PATH_PREFIX));

        CommunityDatabase communityDatabase = new CommunityDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX,
                SIA_SECONDARY_PATH_PREFIX, siaSettings, webService);

        wsParameterProvider.setCommunityDatabase(communityDatabase);
        DatabaseSingleton.setCommunityDatabase(communityDatabase);

        DatabaseSingleton.setFeaturedDatabase(new FeaturedDatabase(
                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX));

        DatabaseSingleton.setCommunityReviewsLoader(
                new CommunityReviewsLoader(webService, "US"));

        DatabaseSingleton.setContactsProvider(number -> {
            if (settings.getUseContacts() && PermissionHelper.hasContactsPermission(context)) {
                String contactName = ContactsHelper.getContactName(context, number);
                if (!TextUtils.isEmpty(contactName)) return new ContactItem(contactName);
            }
            return null;
        });
    }

}
