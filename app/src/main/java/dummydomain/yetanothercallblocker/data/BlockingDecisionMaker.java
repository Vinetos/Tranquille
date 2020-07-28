package dummydomain.yetanothercallblocker.data;

import dummydomain.yetanothercallblocker.Settings;

public class BlockingDecisionMaker implements DatabaseSingleton.BlockingDecisionMaker {

    private final Settings settings;

    public BlockingDecisionMaker(Settings settings) {
        this.settings = settings;
    }

    @Override
    public boolean shouldBlock(NumberInfo numberInfo) {
        if (numberInfo.contactItem != null) return false;

        if (numberInfo.isHiddenNumber && settings.getBlockHiddenNumbers()) {
            return true;
        }

        if (numberInfo.rating == NumberInfo.Rating.NEGATIVE
                && settings.getBlockNegativeSiaNumbers()) {
            return true;
        }

        return false;
    }

}
