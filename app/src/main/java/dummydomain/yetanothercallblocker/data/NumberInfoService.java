package dummydomain.yetanothercallblocker.data;

import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import dummydomain.yetanothercallblocker.Settings;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class NumberInfoService {

    public interface HiddenNumberDetector {
        boolean isHiddenNumber(String number);
    }

    private static final Logger LOG = LoggerFactory.getLogger(NumberInfoService.class);

    protected final Settings settings;

    protected final HiddenNumberDetector hiddenNumberDetector;
    protected final CommunityDatabase communityDatabase;
    protected final FeaturedDatabase featuredDatabase;
    protected final ContactsProvider contactsProvider;
    protected final BlacklistService blacklistService;

    public NumberInfoService(Settings settings, HiddenNumberDetector hiddenNumberDetector,
                             CommunityDatabase communityDatabase, FeaturedDatabase featuredDatabase,
                             ContactsProvider contactsProvider, BlacklistService blacklistService) {
        this.settings = settings;
        this.hiddenNumberDetector = hiddenNumberDetector;
        this.communityDatabase = communityDatabase;
        this.featuredDatabase = featuredDatabase;
        this.contactsProvider = contactsProvider;
        this.blacklistService = blacklistService;
    }

    public NumberInfo getNumberInfo(String number, boolean full) {
        LOG.debug("getNumberInfo({}, {}) started", number, full);
        // TODO: check number format

        NumberInfo numberInfo = new NumberInfo();
        numberInfo.number = number;

        if (hiddenNumberDetector != null) {
            numberInfo.isHiddenNumber = hiddenNumberDetector.isHiddenNumber(number);
        }
        LOG.trace("getNumberInfo() isHiddenNumber={}", numberInfo.isHiddenNumber);

        if (numberInfo.isHiddenNumber || TextUtils.isEmpty(number)
                || TextUtils.getTrimmedLength(number) == 0) {
            numberInfo.noNumber = true;
        }
        LOG.trace("getNumberInfo() noNumber={}", numberInfo.noNumber);

        if (numberInfo.noNumber) {
            numberInfo.blockingReason = getBlockingReason(numberInfo);
            LOG.trace("getNumberInfo() blockingReason={}", numberInfo.blockingReason);
            LOG.debug("getNumberInfo() finished early");
            return numberInfo;
        }

        if (contactsProvider != null) {
            numberInfo.contactItem = contactsProvider.get(number);
        }
        LOG.trace("getNumberInfo() contactItem={}", numberInfo.contactItem);

        if (communityDatabase != null) {
            numberInfo.communityDatabaseItem = communityDatabase.getDbItemByNumber(number);
        }
        LOG.trace("getNumberInfo() communityItem={}", numberInfo.communityDatabaseItem);

        if (featuredDatabase != null) {
            numberInfo.featuredDatabaseItem = featuredDatabase.getDbItemByNumber(number);
        }
        LOG.trace("getNumberInfo() featuredItem={}", numberInfo.featuredDatabaseItem);

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

        if (blacklistService != null && settings.getBlacklistIsNotEmpty()) {
            // avoid loading blacklist if blocking for other reason
            if (full || getBlockingReason(numberInfo) == null) {
                numberInfo.blacklistItem = blacklistService.getBlacklistItemForNumber(number);
            }
        }
        LOG.trace("getNumberInfo() blacklistItem={}", numberInfo.blacklistItem);

        numberInfo.blockingReason = getBlockingReason(numberInfo);
        LOG.trace("getNumberInfo() blockingReason={}", numberInfo.blockingReason);

        LOG.debug("getNumberInfo() finished");
        return numberInfo;
    }

    protected NumberInfo.BlockingReason getBlockingReason(NumberInfo numberInfo) {
        if (numberInfo.contactItem != null) return null;

        if (numberInfo.isHiddenNumber && settings.getBlockHiddenNumbers()) {
            return NumberInfo.BlockingReason.HIDDEN_NUMBER;
        }

        if (numberInfo.rating == NumberInfo.Rating.NEGATIVE
                && settings.getBlockNegativeSiaNumbers()) {
            return NumberInfo.BlockingReason.SIA_RATING;
        }

        if (numberInfo.blacklistItem != null && settings.getBlockBlacklisted()) {
            return NumberInfo.BlockingReason.BLACKLISTED;
        }

        return null;
    }

    public boolean shouldBlock(NumberInfo numberInfo) {
        return numberInfo.blockingReason != null;
    }

    public void blockedCall(NumberInfo numberInfo) {
        if (blacklistService != null && numberInfo.blacklistItem != null
                && numberInfo.blockingReason == NumberInfo.BlockingReason.BLACKLISTED) {
            blacklistService.addCall(numberInfo.blacklistItem, new Date());
        }
    }

}
