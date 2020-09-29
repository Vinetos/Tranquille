package dummydomain.yetanothercallblocker;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import org.greenrobot.greendao.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import dummydomain.yetanothercallblocker.data.db.BlacklistDao;
import dummydomain.yetanothercallblocker.data.db.BlacklistItem;

public class BlacklistDataSource extends PositionalDataSource<BlacklistItem> {

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistDataSource.class);

    public static class Factory extends DataSource.Factory<Integer, BlacklistItem> {
        private final BlacklistDao blacklistDao;

        private volatile BlacklistDataSource ds;

        public Factory(BlacklistDao blacklistDao) {
            this.blacklistDao = blacklistDao;
        }

        public void invalidate() {
            LOG.debug("invalidate()");

            BlacklistDataSource ds = this.ds;
            if (ds != null) ds.invalidate();
        }

        @NonNull
        @Override
        public DataSource<Integer, BlacklistItem> create() {
            return ds = new BlacklistDataSource(blacklistDao);
        }
    }

    private final BlacklistDao blacklistDao;

    private QueryBuilder<BlacklistItem> queryBuilder;

    public BlacklistDataSource(BlacklistDao blacklistDao) {
        this.blacklistDao = blacklistDao;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params,
                            @NonNull LoadInitialCallback<BlacklistItem> callback) {
        LOG.debug("loadInitial({}, {})", params.requestedStartPosition, params.requestedLoadSize);

        List<BlacklistItem> items = getQueryBuilder()
                .offset(params.requestedStartPosition)
                .limit(params.requestedLoadSize)
                .list();

        items = blacklistDao.detach(items); // for DiffUtil to work

        if (params.placeholdersEnabled) {
            callback.onResult(items, params.requestedStartPosition, (int) blacklistDao.countAll());
        } else {
            callback.onResult(items, params.requestedStartPosition);
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
                          @NonNull LoadRangeCallback<BlacklistItem> callback) {
        LOG.debug("loadRange({}, {})", params.startPosition, params.loadSize);

        List<BlacklistItem> items = getQueryBuilder()
                .offset(params.startPosition)
                .limit(params.loadSize)
                .list();

        callback.onResult(blacklistDao.detach(items));
    }

    private QueryBuilder<BlacklistItem> getQueryBuilder() {
        if (queryBuilder == null) {
            queryBuilder = blacklistDao.getDefaultQueryBuilder();
        }
        return queryBuilder;
    }

}
