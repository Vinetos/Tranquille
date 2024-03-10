package fr.vinetos.tranquille.data.db;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.query.CloseableListIterator;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import fr.vinetos.tranquille.BlacklistDataSource;

public class BlacklistDao {

    public interface DaoSessionProvider {
        DaoSession getDaoSession();
    }

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistDao.class);

    private final DaoSessionProvider daoSessionProvider;

    public BlacklistDao(DaoSessionProvider daoSessionProvider) {
        this.daoSessionProvider = daoSessionProvider;
    }

    public BlacklistDataSource.Factory dataSourceFactory() {
        return new BlacklistDataSource.Factory(this);
    }

    public List<BlacklistItem> loadAll() {
        return getDefaultQueryBuilder().list();
    }

    public QueryBuilder<BlacklistItem> getDefaultQueryBuilder() {
        return getBlacklistItemDao().queryBuilder()
                .orderRaw("T.'" + BlacklistItemDao.Properties.Name.columnName + "' IS NULL" +
                        " OR T.'" + BlacklistItemDao.Properties.Name.columnName + "' = ''")
                .orderAsc(BlacklistItemDao.Properties.Name)
                .orderAsc(BlacklistItemDao.Properties.Pattern);
    }

    public <T extends Collection<BlacklistItem>> T detach(T items) {
        BlacklistItemDao dao = getBlacklistItemDao();
        for (BlacklistItem item : items) {
            dao.detach(item);
        }
        return items;
    }

    public BlacklistItem findById(long id) {
        return getBlacklistItemDao().load(id);
    }

    public BlacklistItem findByPattern(String pattern) {
        return first(getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Pattern.eq(pattern))
                .orderAsc(BlacklistItemDao.Properties.Pattern));
    }

    public BlacklistItem findByNameAndPattern(String name, String pattern) {
        return first(getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Name.eq(name))
                .where(BlacklistItemDao.Properties.Pattern.eq(pattern))
                .orderAsc(BlacklistItemDao.Properties.Pattern));
    }

    public void save(BlacklistItem blacklistItem) {
        getBlacklistItemDao().save(blacklistItem);
    }

    public void insert(BlacklistItem blacklistItem) {
        getBlacklistItemDao().insert(blacklistItem);
    }

    public void delete(Iterable<Long> keys) {
        getBlacklistItemDao().deleteByKeyInTx(keys);
    }

    public long countValid() {
        return getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Invalid.notEq(true)).count();
    }

    public BlacklistItem getFirstMatch(String number) {
        return first(getMatchesQueryBuilder(number));
    }

    private QueryBuilder<BlacklistItem> getMatchesQueryBuilder(String number) {
        return getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Invalid.notEq(true),
                        new InverseLikeCondition(BlacklistItemDao.Properties.Pattern, number))
                .orderAsc(BlacklistItemDao.Properties.CreationDate);
    }

    private <T> T first(QueryBuilder<T> queryBuilder) {
        return first(queryBuilder.build());
    }

    private <T> T first(Query<T> query) {
        try (CloseableListIterator<T> it = query.listIterator()) {
            if (it.hasNext()) return it.next();
        } catch (IOException e) {
            LOG.debug("first()", e);
        }
        return null;
    }

    private BlacklistItemDao getBlacklistItemDao() {
        return daoSessionProvider.getDaoSession().getBlacklistItemDao();
    }

    private static class InverseLikeCondition extends WhereCondition.PropertyCondition {
        InverseLikeCondition(Property property, String value) {
            super(property, " ? LIKE ", value);
        }

        @Override
        public void appendTo(StringBuilder builder, String tableAlias) {
            builder.append(op);
            SqlUtils.appendProperty(builder, tableAlias, property);
        }
    }

}
