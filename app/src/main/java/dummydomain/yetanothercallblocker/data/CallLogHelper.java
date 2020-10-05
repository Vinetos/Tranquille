package dummydomain.yetanothercallblocker.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dummydomain.yetanothercallblocker.PermissionHelper;

public class CallLogHelper {

    private static final String[] QUERY_PROJECTION = new String[]{
            CallLog.Calls._ID, CallLog.Calls.TYPE, CallLog.Calls.NUMBER,
            CallLog.Calls.DATE, CallLog.Calls.DURATION
    };

    public static List<CallLogItem> loadCalls(Context context, Long anchorId, boolean before,
                                              int limit) {
        if (!PermissionHelper.hasCallLogPermission(context)) {
            return new ArrayList<>();
        }

        boolean reverseOrder = false;

        String selection;
        String[] selectionArgs;
        if (anchorId != null) {
            if (before) {
                selection = CallLog.Calls._ID + " > ?";
                reverseOrder = true;
            } else {
                selection = CallLog.Calls._ID + " < ?";
            }
            selectionArgs = new String[]{String.valueOf(anchorId)};
        } else {
            selection = null;
            selectionArgs = null;
        }

        String sortOrder = CallLog.Calls.DATE + " " + (reverseOrder ? "ASC" : "DESC");

        sortOrder += " limit " + limit;

        List<CallLogItem> items = new ArrayList<>(limit);

        try (Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                QUERY_PROJECTION, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                int idIndex = cursor.getColumnIndex(CallLog.Calls._ID);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idIndex);
                    int callType = cursor.getInt(typeIndex);
                    String number = cursor.getString(numberIndex);
                    long callDate = cursor.getLong(dateIndex);
                    long callDuration = cursor.getLong(durationIndex);

                    items.add(new CallLogItem(id, CallLogItem.Type.fromProviderType(callType),
                            number, callDate, callDuration));
                }
            }
        }

        if (reverseOrder) {
            Collections.reverse(items);
        }

        return items;
    }

}
