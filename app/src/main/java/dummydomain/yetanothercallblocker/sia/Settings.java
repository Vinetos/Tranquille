package dummydomain.yetanothercallblocker.sia;

public interface Settings {

    int getBaseDbVersion();

    void setBaseDbVersion(int version);

    int getSecondaryDbVersion();

    void setSecondaryDbVersion(int version);

}
