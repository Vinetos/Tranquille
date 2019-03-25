package dummydomain.yetanothercallblocker.sia.model.database;

public class FeaturedDatabaseItem {

    private long number;

    private String name;

    public FeaturedDatabaseItem(long number, String name) {
        this.number = number;
        this.name = name;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FeaturedDatabaseItem{" +
                "number=" + number +
                ", name='" + name + '\'' +
                '}';
    }

}
