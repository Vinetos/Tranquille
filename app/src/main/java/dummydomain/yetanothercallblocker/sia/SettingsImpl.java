package dummydomain.yetanothercallblocker.sia;

public class SettingsImpl implements Settings {

    private static final String PREF_BASE_DB_VERSION = "baseDbVersion";
    private static final String PREF_SECONDARY_DB_VERSION = "secondaryDbVersion";

    private final Properties props;

    public SettingsImpl(Properties props) {
        this.props = props;
    }

    @Override
    public int getBaseDbVersion() {
        return props.getInt(PREF_BASE_DB_VERSION, 0);
    }

    @Override
    public void setBaseDbVersion(int version) {
        props.setInt(PREF_BASE_DB_VERSION, version);
    }

    @Override
    public int getSecondaryDbVersion() {
        return props.getInt(PREF_SECONDARY_DB_VERSION, 0);
    }

    @Override
    public void setSecondaryDbVersion(int version) {
        props.setInt(PREF_SECONDARY_DB_VERSION, version);
    }

}
