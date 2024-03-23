package fr.vinetos.tranquille.data;

public interface ContactsProvider {

    ContactItem get(String number);

    boolean isInLimitedMode();

}
