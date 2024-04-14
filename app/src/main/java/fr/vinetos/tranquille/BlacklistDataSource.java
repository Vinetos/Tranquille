package fr.vinetos.tranquille;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import fr.vinetos.tranquille.data.DenylistItem;
import fr.vinetos.tranquille.data.db.DenylistDataSource;

public class BlacklistDataSource extends PositionalDataSource<DenylistItem> {

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistDataSource.class);

    public static class Factory extends DataSource.Factory<Integer, DenylistItem> {
        private final DenylistDataSource denylistDataSource;

        private volatile BlacklistDataSource ds;

        public Factory(DenylistDataSource denylistDataSource) {
            this.denylistDataSource = denylistDataSource;
        }

        public BlacklistDataSource getCurrentDataSource() {
            return ds;
        }

        public void invalidate() {
            LOG.debug("invalidate()");

            BlacklistDataSource ds = this.ds;
            if (ds != null) ds.invalidate();
        }

        @NonNull
        @Override
        public DataSource<Integer, DenylistItem> create() {
            return ds = new BlacklistDataSource(denylistDataSource);
        }
    }

    private final DenylistDataSource denylistDataSource;

    public BlacklistDataSource(DenylistDataSource denylistDataSource) {
        this.denylistDataSource = denylistDataSource;
    }

    /**
     * The iterable must be iterated through
     * or the underlying cursor won't be closed.
     *
     * @return an iterable containing ids of all items this DS would load
     */
    public Iterable<Long> getAllIds() {
        Iterator<DenylistItem> iterator = getQueryBuilder().listIterator();
        return () -> new Iterator<Long>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Long next() {
                return iterator.next().getId();
            }
        };
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params,
                            @NonNull LoadInitialCallback<DenylistItem> callback) {
        LOG.debug("loadInitial({}, {})", params.requestedStartPosition, params.requestedLoadSize);

        int offset = params.requestedStartPosition;

        List<DenylistItem> items = loadItems(offset, params.requestedLoadSize);

        Integer totalCount = null;

        if (items.isEmpty()) {
            totalCount = (int) countAll();
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
            if (totalCount == null) totalCount = (int) countAll();

            callback.onResult(items, offset, totalCount);
        } else {
            callback.onResult(items, offset);
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
                          @NonNull LoadRangeCallback<DenylistItem> callback) {
        LOG.debug("loadRange({}, {})", params.startPosition, params.loadSize);

        callback.onResult(loadItems(params.startPosition, params.loadSize));
    }

    private List<DenylistItem> loadItems(int offset, int limit) {

        List<DenylistItem> items = getQueryBuilder()
                .offset(offset)
                .limit(limit)
                .list();

        return denylistDataSource.detach(items); // for DiffUtil to work
    }

    private long countAll() {
        return denylistDataSource.countAll();
    }

}
