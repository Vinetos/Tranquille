package dummydomain.yetanothercallblocker.data;

import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

import static dummydomain.yetanothercallblocker.sia.SiaConstants.SIA_PATH_PREFIX;
import static dummydomain.yetanothercallblocker.sia.SiaConstants.SIA_SECONDARY_PATH_PREFIX;

public class DatabaseSingleton { // TODO: rewrite

    private static final CommunityDatabase COMMUNITY_DATABASE = new CommunityDatabase(SIA_PATH_PREFIX, SIA_SECONDARY_PATH_PREFIX);
    private static final FeaturedDatabase FEATURED_DATABASE = new FeaturedDatabase(SIA_PATH_PREFIX);

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSingleton.class);

    private static ContactsProvider contactsProvider;

    public static CommunityDatabase getCommunityDatabase() {
        return COMMUNITY_DATABASE;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return FEATURED_DATABASE;
    }

    public static void setContactsProvider(ContactsProvider contactsProvider) {
        DatabaseSingleton.contactsProvider = contactsProvider;
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

        ContactItem contactItem = contactsProvider != null ? contactsProvider.get(number) : null;
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
