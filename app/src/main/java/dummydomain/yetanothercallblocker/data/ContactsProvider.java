package dummydomain.yetanothercallblocker.data;

public interface ContactsProvider {

    ContactItem get(String number);

    boolean isInLimitedMode();

}
