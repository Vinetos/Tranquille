package dummydomain.yetanothercallblocker.sia.model;

import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;

public class NumberInfo {

    public enum Rating {
        POSITIVE, NEUTRAL, NEGATIVE, UNKNOWN
    }

    public String number;
    public String name;
    public boolean known;
    public Rating rating = Rating.UNKNOWN;
    public CommunityDatabaseItem communityDatabaseItem;

}
