package dummydomain.yetanothercallblocker.data;

public class ContactItem {

    public String displayName;

    public ContactItem(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "displayName='" + displayName + '\'' +
                '}';
    }
}
