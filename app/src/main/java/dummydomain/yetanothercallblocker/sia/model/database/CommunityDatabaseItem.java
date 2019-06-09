package dummydomain.yetanothercallblocker.sia.model.database;

import dummydomain.yetanothercallblocker.sia.model.NumberCategory;

public class CommunityDatabaseItem {

    private long number;

    private int positiveRatingsCount;
    private int negativeRatingsCount;
    private int neutralRatingsCount;

    private int unknownData;

    private int category;

    public long getNumber() {
        return number;
    }

    public void setNumber(long j) {
        this.number = j;
    }

    public int getPositiveRatingsCount() {
        return this.positiveRatingsCount;
    }

    public void setPositiveRatingsCount(int c) {
        this.positiveRatingsCount = c;
    }

    public int getNegativeRatingsCount() {
        return this.negativeRatingsCount;
    }

    public void setNegativeRatingsCount(int c) {
        this.negativeRatingsCount = c;
    }

    public int getNeutralRatingsCount() {
        return this.neutralRatingsCount;
    }

    public void setNeutralRatingsCount(int c) {
        this.neutralRatingsCount = c;
    }

    public int getUnknownData() {
        return unknownData;
    }

    public void setUnknownData(int v) {
        this.unknownData = v;
    }

    public int getCategory() {
        return this.category;
    }

    public void setCategory(int c) {
        this.category = c;
    }

    public boolean hasRatings() {
        return positiveRatingsCount + negativeRatingsCount + negativeRatingsCount > 0;
    }

    @Override
    public String toString() {
        return "CommunityDatabaseItem{" +
                "number=" + number +
                ", positiveRatingsCount=" + positiveRatingsCount +
                ", negativeRatingsCount=" + negativeRatingsCount +
                ", neutralRatingsCount=" + neutralRatingsCount +
                ", unknownData=" + unknownData +
                ", category=" + NumberCategory.getById(category) +
                '}';
    }

}
