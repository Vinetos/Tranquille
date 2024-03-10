package fr.vinetos.tranquille.data.db;

import android.content.Context;

public class YacbDaoSessionFactory {

    private final Context context;
    private final String dbName;

    private final Object lock = new Object();
    private DaoSession daoSession;

    public YacbDaoSessionFactory(Context context, String dbName) {
        this.context = context;
        this.dbName = dbName;
    }

    public DaoSession getDaoSession() {
        DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            synchronized (lock) {
                daoSession = this.daoSession;
                if (daoSession == null) {
                    this.daoSession = daoSession = initDaoSession();
                }
            }
        }
        return daoSession;
    }

    private DaoSession initDaoSession() {
        YacbDbOpenHelper dbOpenHelper = new YacbDbOpenHelper(context, dbName);
        return new DaoMaster(dbOpenHelper.getWritableDb()).newSession();
    }

}
