package dummydomain.yetanothercallblocker.sia;

import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabase;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabase;

public class DatabaseSingleton {

    private static final CommunityDatabase COMMUNITY_DATABASE = new CommunityDatabase();
    private static final FeaturedDatabase FEATURED_DATABASE = new FeaturedDatabase();

    public static CommunityDatabase getCommunityDatabase() {
        return COMMUNITY_DATABASE;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return FEATURED_DATABASE;
    }

}
