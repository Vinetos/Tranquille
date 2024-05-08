package fr.vinetos.tranquille.domain.service;

import android.text.TextUtils;

import java.util.Date;

import fr.vinetos.tranquille.EventUtils;
import fr.vinetos.tranquille.data.BlacklistUtils;
import fr.vinetos.tranquille.data.DenylistItem;
import fr.vinetos.tranquille.data.datasource.DenylistDataSource;
import fr.vinetos.tranquille.event.BlacklistChangedEvent;
import fr.vinetos.tranquille.event.BlacklistItemChangedEvent;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;

public class DenylistService {

    public interface Callback {
        void changed(boolean notEmpty);
    }

    private final Callback callback;
    private final DenylistDataSource denylistDataSource;

    public DenylistService(Callback callback, DenylistDataSource denylistDataSource) {
        this.callback = callback;
        this.denylistDataSource = denylistDataSource;
    }

    public DenylistItem getDenylistItemForNumber(String number) {
        if (TextUtils.isEmpty(number)) return null;

        number = BlacklistUtils.cleanNumber(number);

        return denylistDataSource.getFirstMatch(number);
    }

    public boolean insert(String name, String pattern) {
        // Name is optional
        if(name == null)
            name = "";

        if(TextUtils.isEmpty(pattern))
            throw new NullPointerException("Pattern cannot be null or empty");
        pattern = BlacklistUtils.cleanPattern(pattern);

        if(!BlacklistUtils.isValidPattern(pattern))
            throw new IllegalArgumentException("Pattern is not valid");

        try {
            String finalName = name;
            String cleanedPattern = pattern;
            BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> denylistDataSource.save(new DenylistItem(
                            -1,
                            finalName,
                            cleanedPattern,
                            new Date().toString(),
                            0,
                            0,
                            null
                    ), continuation)
            );
            blacklistChanged(false);
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
                    (scope, continuation) -> denylistDataSource.update(denylistItem, continuation)
            );
            blacklistChanged(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCall(DenylistItem denylistItem, Date date) {
        sanitize(denylistItem);

        try {
            BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> denylistDataSource.addCall(denylistItem, date.toString(), continuation)
            );

            EventUtils.postEvent(new BlacklistItemChangedEvent());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(Iterable<Long> keys) {
        try {
            BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> denylistDataSource.delete(keys.iterator(), continuation)
            );

            blacklistChanged(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void sanitize(DenylistItem denylistItem) {
        final String creationDate = denylistItem.getCreationDate().isEmpty() ? new Date().toString() : denylistItem.getCreationDate();
        final long numberOfCalls = denylistItem.getNumberOfCalls() < 0 ? 0 : denylistItem.getNumberOfCalls();
        try {
            BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> denylistDataSource.sanitize(denylistItem, !BlacklistUtils.isValidPattern(denylistItem.getPattern()), creationDate, numberOfCalls, continuation)
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void blacklistChanged(boolean itemUpdate) {
        callback.changed(denylistDataSource.countValid() != 0);

        EventUtils.postEvent(itemUpdate ? new BlacklistItemChangedEvent() : new BlacklistChangedEvent());
    }

}
