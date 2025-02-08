package fr.vinetos.tranquille.domain.service;

import android.text.TextUtils;

import java.util.Date;

import fr.vinetos.tranquille.EventUtils;
import fr.vinetos.tranquille.data.DenylistUtils;
import fr.vinetos.tranquille.data.DenylistItem;
import fr.vinetos.tranquille.DenylistDataSource;
import fr.vinetos.tranquille.data.datasource.DenylistDao;
import fr.vinetos.tranquille.event.DenylistChangedEvent;
import fr.vinetos.tranquille.event.DenylistItemChangedEvent;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;

public class DenylistService {

    public interface Callback {
        void changed(boolean notEmpty);
    }

    private final Callback callback;
    private final DenylistDao denylistDao;

    public DenylistService(Callback callback, DenylistDao denylistDao) {
        this.callback = callback;
        this.denylistDao = denylistDao;
    }

    public DenylistItem getDenylistItemForNumber(String number) {
        if (TextUtils.isEmpty(number)) return null;

        number = DenylistUtils.cleanNumber(number);

        return denylistDao.getFirstMatch(number);
    }

    public boolean insert(DenylistItem denylistItem) {
        sanitize(denylistItem);
        try {
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.insert(denylistItem, continuation)
            );
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            return false;
        }

        denylistChanged(false);
        return true;
    }

    public boolean insert(String name, String pattern) {
        // Name is optional
        if (name == null)
            name = "";

        if (TextUtils.isEmpty(pattern))
            throw new NullPointerException("Pattern cannot be null or empty");
        pattern = DenylistUtils.cleanPattern(pattern);

        if (!DenylistUtils.isValidPattern(pattern))
            throw new IllegalArgumentException("Pattern is not valid");

        try {
            String finalName = name;
            String cleanedPattern = pattern;
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.insert(
                    new DenylistItem(
                        -1L,
                        finalName,
                        cleanedPattern,
                        new Date(),
                        false,
                        0,
                        null
                    ),
                    continuation
                )
            );
            denylistChanged(false);
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            return false;
        }
        return true;
    }

    public void update(DenylistItem denylistItem) {
        sanitize(denylistItem);
        try {
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.update(denylistItem, continuation)
            );
            denylistChanged(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCall(DenylistItem denylistItem, Date date) {
        sanitize(denylistItem);

        try {
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.addCall(denylistItem, date, continuation)
            );

            EventUtils.postEvent(new DenylistItemChangedEvent());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(Iterable<Long> keys) {
        try {
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.delete(keys.iterator(), continuation)
            );

            denylistChanged(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sanitize(DenylistItem denylistItem) {
        final long numberOfCalls = denylistItem.getNumberOfCalls() < 0 ? 0 : denylistItem.getNumberOfCalls();
        try {
            BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> denylistDao.sanitize(
                    denylistItem,
                    !DenylistUtils.isValidPattern(denylistItem.getPattern()),
                    denylistItem.getCreationDate(),
                    numberOfCalls,
                    continuation
                )
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void denylistChanged(boolean itemUpdate) {
        callback.changed(denylistDao.countValid() != 0);

        EventUtils.postEvent(itemUpdate ? new DenylistItemChangedEvent() : new DenylistChangedEvent());
    }

}
