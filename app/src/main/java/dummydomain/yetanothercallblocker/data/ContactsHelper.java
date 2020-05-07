package dummydomain.yetanothercallblocker.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import dummydomain.yetanothercallblocker.PermissionHelper;
import dummydomain.yetanothercallblocker.Settings;

public class ContactsHelper {

    private static final String[] PROJECTION = new String[]{
            ContactsContract.PhoneLookup.DISPLAY_NAME
    };

    public static ContactsProvider getContactsProvider(Context context, Settings settings) {
        return number -> {
            if (settings.getUseContacts() && PermissionHelper.hasContactsPermission(context)) {
                String contactName = getContactName(context, number);
                if (!TextUtils.isEmpty(contactName)) return new ContactItem(contactName);
            }
            return null;
        };
    }

    public static String getContactName(Context context, String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(uri, PROJECTION, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(
                        ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        }

        return null;
    }

}
