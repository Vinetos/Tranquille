package dummydomain.yetanothercallblocker.sia.model;

public enum NumberRating {

    UNKNOWN(0),
    POSITIVE(1),
    NEGATIVE(2),
    NEUTRAL(3);

    private int id;

    NumberRating(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static NumberRating getById(int id) {
        for (NumberRating rating : values()) {
            if (rating.getId() == id) return rating;
        }
        return null;
    }

}
