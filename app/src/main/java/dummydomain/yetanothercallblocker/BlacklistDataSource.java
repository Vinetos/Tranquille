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

        int offset = params.requestedStartPosition;

        List<BlacklistItem> items = loadItems(offset, params.requestedLoadSize);

        Integer totalCount = null;

        if (items.isEmpty()) {
            totalCount = (int) blacklistDao.countAll();
            if (totalCount > 0) {
                LOG.debug("loadInitial() initial range is empty: totalCount={}, offset={}",
                        totalCount, offset);

                offset = (totalCount - params.requestedLoadSize)
                        / params.pageSize * params.pageSize; // align to pageSize using integer math

                if (offset < 0) offset = 0;

                LOG.debug("loadInitial() reloading with offset={}", offset);
                items = loadItems(offset, params.requestedLoadSize);
            } else {
                offset = 0;
            }
        }

        if (params.placeholdersEnabled) {
            if (totalCount == null) totalCount = (int) blacklistDao.countAll();

            callback.onResult(items, offset, totalCount);
        } else {
            callback.onResult(items, offset);
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
                          @NonNull LoadRangeCallback<BlacklistItem> callback) {
        LOG.debug("loadRange({}, {})", params.startPosition, params.loadSize);

        callback.onResult(loadItems(params.startPosition, params.loadSize));
    }

    private List<BlacklistItem> loadItems(int offset, int limit) {
        List<BlacklistItem> items = getQueryBuilder()
                .offset(offset)
                .limit(limit)
                .list();

        return blacklistDao.detach(items); // for DiffUtil to work
    }

    private QueryBuilder<BlacklistItem> getQueryBuilder() {
        if (queryBuilder == null) {
            queryBuilder = blacklistDao.getDefaultQueryBuilder();
        }
        return queryBuilder;
    }

}
