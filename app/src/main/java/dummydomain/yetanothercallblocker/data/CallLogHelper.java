package dummydomain.yetanothercallblocker.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.List;

public class CallLogHelper {

    private static final String[] QUERY_PROJECTION = new String[]{
            CallLog.Calls.TYPE, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION
    };

    public static List<CallLogItem> getRecentCalls(Context context, int num) {
        List<CallLogItem> logItems = new ArrayList<>(num);

        try (Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                QUERY_PROJECTION, null, null, CallLog.Calls.DEFAULT_SORT_ORDER)) {
            if (cursor != null) {
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                while (cursor.moveToNext() && logItems.size() < num) {
                    int callType = cursor.getInt(typeIndex);
                    String number = cursor.getString(numberIndex);
                    long callDate = cursor.getLong(dateIndex);
                    long callDuration = cursor.getLong(durationIndex);

                    logItems.add(new CallLogItem(CallLogItem.Type.fromProviderType(callType),
                            number, callDate, callDuration));
                }
            }
        }

        return logItems;
    }

}
