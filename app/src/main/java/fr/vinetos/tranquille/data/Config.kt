package fr.vinetos.tranquille.data

import android.content.Context
import android.os.Build
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dummydomain.yetanothercallblocker.sia.SettingsImpl
import dummydomain.yetanothercallblocker.sia.Storage
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata
import dummydomain.yetanothercallblocker.sia.model.SiaMetadata.Country
import dummydomain.yetanothercallblocker.sia.model.database.AbstractDatabase
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase
import dummydomain.yetanothercallblocker.sia.model.database.DbManager
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase
import dummydomain.yetanothercallblocker.sia.network.DbDownloader
import dummydomain.yetanothercallblocker.sia.network.DbUpdateRequester
import dummydomain.yetanothercallblocker.sia.network.OkHttpClientFactory
import dummydomain.yetanothercallblocker.sia.network.WebService
import dummydomain.yetanothercallblocker.sia.network.WebService.DefaultWSParameterProvider
import dummydomain.yetanothercallblocker.sia.utils.Utils
import fr.vinetos.tranquille.BuildConfig
import fr.vinetos.tranquille.NotificationService
import fr.vinetos.tranquille.PhoneStateHandler
import fr.vinetos.tranquille.Settings
import fr.vinetos.tranquille.data.Database.Companion.Schema
import fr.vinetos.tranquille.data.Database.Companion.invoke
import fr.vinetos.tranquille.data.datasource.DateColumnAdapter
import fr.vinetos.tranquille.DenylistDataSource
import fr.vinetos.tranquille.data.datasource.DenylistDao
import fr.vinetos.tranquille.domain.service.DenylistService
import fr.vinetos.tranquille.utils.DbFilteringUtils
import fr.vinetos.tranquille.utils.DeferredInit
import fr.vinetos.tranquille.utils.SystemUtils
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

object Config {
    @JvmStatic
    fun init(context: Context, settings: Settings) {
        val storage: Storage = AndroidStorage(context)
        val siaSettings
                : dummydomain.yetanothercallblocker.sia.Settings =
            SettingsImpl(AndroidProperties(context, SiaConstants.SIA_PROPERTIES))

        val okHttpClientFactory = OkHttpClientFactory {
            DeferredInit.initNetwork()
            OkHttpClient()
        }

        val communityDatabase = CommunityDatabase(
            storage, AbstractDatabase.Source.ANY, SiaConstants.SIA_PATH_PREFIX,
            SiaConstants.SIA_SECONDARY_PATH_PREFIX, siaSettings
        )
        YacbHolder.setCommunityDatabase(communityDatabase)

        val siaMetadata = SiaMetadata(
            storage, SiaConstants.SIA_PATH_PREFIX
        ) { communityDatabase.isUsingInternal }
        YacbHolder.setSiaMetadata(siaMetadata)

        val featuredDatabase = FeaturedDatabase(
            storage, AbstractDatabase.Source.ANY, SiaConstants.SIA_PATH_PREFIX
        )
        YacbHolder.setFeaturedDatabase(featuredDatabase)

        val wsParameterProvider = WSParameterProvider(
            settings, siaMetadata, communityDatabase
        )

        val webService = WebService(wsParameterProvider, okHttpClientFactory)
        YacbHolder.setWebService(webService)

        YacbHolder.setDbManager(
            DbManager(
                storage, SiaConstants.SIA_PATH_PREFIX,
                DbDownloader(okHttpClientFactory), DbUpdateRequester(webService),
                communityDatabase
            )
        )

        YacbHolder.getDbManager().setNumberFilter(DbFilteringUtils.getNumberFilter(settings))

        YacbHolder.setCommunityReviewsLoader(CommunityReviewsLoader(webService))

        val driver: SqlDriver = AndroidSqliteDriver(
            schema = Schema,
            context = context,
            name = "tranquille.db",
            factory = if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Support database inspector in Android Studio
                FrameworkSQLiteOpenHelperFactory()
            } else {
                RequerySQLiteOpenHelperFactory()
            }
        )
        val denylistDao = DenylistDao(
            invoke(
                driver,
                DenylistItem.Adapter(
                    DateColumnAdapter,
                    DateColumnAdapter
                )
            )
        )

        YacbHolder.setDenylistDao(denylistDao)

        val denylistService = DenylistService(
            { flag: Boolean -> settings.denylistIsNotEmpty = flag }, denylistDao
        )
        YacbHolder.setDenylistService(denylistService)

        val contactsProvider: ContactsProvider = object : ContactsProvider {
            override fun get(number: String): ContactItem? {
                return if (settings.useContacts) {
                    ContactsHelper.getContact(
                        context,
                        number
                    )
                } else {
                    null
                }
            }

            override fun isInLimitedMode(): Boolean {
                return !SystemUtils.isUserUnlocked(context)
            }
        }

        val numberInfoService = NumberInfoService(
            settings,
            { number: String? -> NumberUtils.isHiddenNumber(number) },
            { number: String?, countryCode: String? ->
                NumberUtils.normalizeNumber(
                    number,
                    countryCode
                )
            },
            communityDatabase, featuredDatabase, contactsProvider, denylistService
        )
        YacbHolder.setNumberInfoService(numberInfoService)

        val notificationService = NotificationService(context)
        YacbHolder.setNotificationService(notificationService)

        YacbHolder.setPhoneStateHandler(
            PhoneStateHandler(context, settings, numberInfoService, notificationService)
        )
    }

    private class WSParameterProvider(
        val settings: Settings,
        val siaMetadata: SiaMetadata,
        val communityDatabase: CommunityDatabase
    ) : DefaultWSParameterProvider() {
        @Volatile
        var storedAppId: String? = null

        override fun getAppId(): String {
            var appId = storedAppId
            if (appId != null && System.nanoTime() >
                appIdTimestamp + TimeUnit.MINUTES.toNanos(5)
            ) {
                appId = null
            }

            if (appId == null) {
                appId = Utils.generateAppId()
                this.storedAppId = appId
                appIdTimestamp = System.nanoTime()
            }

            return appId!!
        }

        @Volatile
        var appIdTimestamp: Long = 0

        override fun getAppVersion(): Int {
            return siaMetadata.siaAppVersion
        }

        override fun getOkHttpVersion(): String {
            return siaMetadata.siaOkHttpVersion
        }

        override fun getDbVersion(): Int {
            return communityDatabase.effectiveDbVersion
        }

        override fun getCountry(): Country {
            return siaMetadata.getCountry(settings.countryCode)
        }
    }
}
