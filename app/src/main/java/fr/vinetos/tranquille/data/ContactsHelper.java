package fr.vinetos.tranquille.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import fr.vinetos.tranquille.PermissionHelper;

public class ContactsHelper {

    private static final String[] PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
    };

    public static ContactItem getContact(Context context, String number) {
        if (TextUtils.isEmpty(number)) return null;
        if (!PermissionHelper.hasContactsPermission(context)) return null;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(uri, PROJECTION, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                ContactItem contact = new ContactItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(
                                ContactsContract.Contacts._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(
                                ContactsContract.Contacts.DISPLAY_NAME))
                );

                if (TextUtils.isEmpty(contact.displayName)) return null; // TODO: check

                return contact;
            }
        }

        return null;
    }

}
