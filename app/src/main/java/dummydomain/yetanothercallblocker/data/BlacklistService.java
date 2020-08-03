package dummydomain.yetanothercallblocker.data;

import android.text.TextUtils;

import java.util.Date;
import java.util.Objects;

import dummydomain.yetanothercallblocker.data.db.BlacklistDao;
import dummydomain.yetanothercallblocker.data.db.BlacklistItem;
import dummydomain.yetanothercallblocker.event.BlacklistChangedEvent;
import dummydomain.yetanothercallblocker.event.BlacklistItemChangedEvent;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;

public class BlacklistService {

    public interface Callback {
        void changed(boolean notEmpty);
    }

    private final Callback callback;
    private final BlacklistDao blacklistDao;

    public BlacklistService(Callback callback, BlacklistDao blacklistDao) {
        this.callback = callback;
        this.blacklistDao = blacklistDao;
    }

    public BlacklistItem getBlacklistItemForNumber(String number) {
        if (TextUtils.isEmpty(number)) return null;

        number = BlacklistUtils.cleanNumber(number);

        return blacklistDao.getFirstMatch(number);
    }

    public void save(BlacklistItem blacklistItem) {
        boolean newItem = blacklistItem.getId() == null;

        blacklistItem.setInvalid(!BlacklistUtils.isValidPattern(blacklistItem.getPattern()));
        blacklistDao.save(blacklistItem);

        blacklistChanged();

        postEvent(newItem ? new BlacklistChangedEvent() : new BlacklistItemChangedEvent());
    }

    public void addCall(BlacklistItem blacklistItem, Date date) {
        blacklistItem.setLastCallDate(Objects.requireNonNull(date));
        blacklistItem.setNumberOfCalls(blacklistItem.getNumberOfCalls() + 1);
        blacklistDao.save(blacklistItem);

        postEvent(new BlacklistItemChangedEvent());
    }

    public void delete(Iterable<Long> keys) {
        blacklistDao.delete(keys);

        blacklistChanged();
    }

    private void blacklistChanged() {
        callback.changed(blacklistDao.countValid() != 0);
    }

}
