package dummydomain.yetanothercallblocker.sia;

import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;

import static dummydomain.yetanothercallblocker.sia.SiaConstants.*;

public class DatabaseSingleton {

    private static final CommunityDatabase COMMUNITY_DATABASE = new CommunityDatabase(SIA_PATH_PREFIX, SIA_SECONDARY_PATH_PREFIX);
    private static final FeaturedDatabase FEATURED_DATABASE = new FeaturedDatabase(SIA_PATH_PREFIX);

    public static CommunityDatabase getCommunityDatabase() {
        return COMMUNITY_DATABASE;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return FEATURED_DATABASE;
    }

}
