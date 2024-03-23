package fr.vinetos.tranquille.data;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.vinetos.tranquille.App;

import static fr.vinetos.tranquille.data.CallLogHelper.loadCalls;

public class CallLogDataSource extends ItemKeyedDataSource<CallLogDataSource.GroupId, CallLogItemGroup> {

    private static final Logger LOG = LoggerFactory.getLogger(CallLogDataSource.class);

    public static class Factory extends DataSource.Factory<GroupId, CallLogItemGroup> {
        private Function<List<CallLogItem>, List<CallLogItemGroup>> groupConverter;

        private volatile CallLogDataSource ds;

        public Factory(Function<List<CallLogItem>, List<CallLogItemGroup>> groupConverter) {
            this.groupConverter = groupConverter;
        }

        public void setGroupConverter(Function<List<CallLogItem>, List<CallLogItemGroup>> converter) {
            this.groupConverter = converter;
        }

        public void invalidate() {
            LOG.debug("invalidate()");

            CallLogDataSource ds = this.ds;
            if (ds != null) ds.invalidate();
        }

        @NonNull
        @Override
        public DataSource<GroupId, CallLogItemGroup> create() {
            return ds = new CallLogDataSource(groupConverter);
        }
    }

    public static class GroupId {
        private static final String KEY_FIRST = "CallLogDataSource.ComplexId.first";
        private static final String KEY_LAST = "CallLogDataSource.ComplexId.last";

        final long firstId, lastId;

        GroupId(long firstId, long lastId) {
            this.firstId = firstId;
            this.lastId = lastId;
        }

        public static GroupId fromParcelable(@Nullable Parcelable parcelable) {
            if (parcelable instanceof Bundle) {
                Bundle bundle = (Bundle) parcelable;
                if (bundle.containsKey(KEY_FIRST) && bundle.containsKey(KEY_LAST)) {
                    return new GroupId(bundle.getLong(KEY_FIRST), bundle.getLong(KEY_LAST));
                }
            }
            return null;
        }

        public Parcelable saveInstanceState() {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_FIRST, firstId);
            bundle.putLong(KEY_LAST, lastId);
            return bundle;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return "ComplexId{" +
                    "firstId=" + firstId +
                    ", lastId=" + lastId +
                    '}';
        }
    }

    private final Function<List<CallLogItem>, List<CallLogItemGroup>> groupConverter;

    private final Map<String, NumberInfo> numberInfoCache = new HashMap<>();

    public CallLogDataSource(Function<List<CallLogItem>, List<CallLogItemGroup>> groupConverter) {
        this.groupConverter = groupConverter;
    }

    @NonNull
    @Override
    public GroupId getKey(@NonNull CallLogItemGroup group) {
        List<CallLogItem> items = group.getItems();
        return new GroupId(items.get(0).id, items.get(items.size() - 1).id);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<GroupId> params,
                            @NonNull LoadInitialCallback<CallLogItemGroup> callback) {
        LOG.debug("loadInitial({}, {})", params.requestedInitialKey, params.requestedLoadSize);

        int size = params.requestedLoadSize * 3 / 2; // compensate for grouping

        List<CallLogItem> items;

        if (params.requestedInitialKey != null) {
            // load something or the list will be empty

            items = new ArrayList<>(size);
            items.addAll(loadCalls(getContext(), params.requestedInitialKey.firstId, true, size / 2));
            items.addAll(loadCalls(getContext(), params.requestedInitialKey.firstId + 1, false, size / 2));
        } else {
            items = loadCalls(getContext(), null, false, size);
        }

        callback.onResult(groupConverter.apply(loadInfo(items)));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<GroupId> params,
                           @NonNull LoadCallback<CallLogItemGroup> callback) {
        LOG.debug("loadBefore({}, {})", params.key, params.requestedLoadSize);

        int size = params.requestedLoadSize * 3 / 2; // compensate for grouping

        List<CallLogItem> items = loadCalls(getContext(), params.key.firstId, true, size);

        callback.onResult(groupConverter.apply(loadInfo(items)));
    }

    @Override
    public void loadAfter(@NonNull LoadParams<GroupId> params,
                          @NonNull LoadCallback<CallLogItemGroup> callback) {
        LOG.debug("loadAfter({}, {})", params.key, params.requestedLoadSize);

        int size = params.requestedLoadSize * 3 / 2; // compensate for grouping

        List<CallLogItem> items = loadCalls(getContext(), params.key.lastId, false, size);

        callback.onResult(groupConverter.apply(loadInfo(items)));
    }

    private List<CallLogItem> loadInfo(List<CallLogItem> items) {
        String countryCode = App.getSettings().getCachedAutoDetectedCountryCode();

        for (CallLogItem item : items) {
            NumberInfo numberInfo = numberInfoCache.get(item.number);
            if (numberInfo == null) {
                numberInfo = YacbHolder.getNumberInfo(item.number, countryCode);
                numberInfoCache.put(item.number, numberInfo);
            }

            item.numberInfo = numberInfo;
        }

        return items;
    }

    private Context getContext() {
        return App.getInstance();
    }

}
