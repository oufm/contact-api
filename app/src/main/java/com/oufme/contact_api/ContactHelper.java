package com.oufme.contact_api;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactHelper {
    public String accountType = "";
    public String accountName = "";
    Context context = null;

    public ContactHelper(Context context) {
        this.context = context;
        getDefaultAccountNameAndType();
    }

    public void getDefaultAccountNameAndType() {
        long rawContactId = 0;
        Uri rawContactUri = null;
        ContentProviderResult[] results = null;

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .build());

        try {
            results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ops.clear();
        }

        for (ContentProviderResult result : results) {
            rawContactUri = result.uri;
            rawContactId = ContentUris.parseId(rawContactUri);
        }

        Cursor c = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI
                , new String[] {ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.RawContacts.ACCOUNT_NAME}
                , ContactsContract.RawContacts._ID+"=?"
                , new String[] {String.valueOf(rawContactId)}
                , null);

        if(c.moveToFirst()) {
            if(!c.isAfterLast()) {
                accountType = c.getString(
                        c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                accountName = c.getString(
                        c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
            }
        }

        context.getContentResolver().delete(rawContactUri, null, null);

        c.close();
        c = null;
    }

    public void addContact(String name, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Adding insert operation to operations list
        // to insert a new raw contact in the ContactsContract.RawContacts table
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                .build());

        // Adding insert operation to operations list
        // to insert display name in the ContactsContract.Data table
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // Adding insert operation to operations list
        // to insert phone number in the ContactsContract.Data table
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Applying the operations
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("ContactHelper", "Add contact " + name + ":" + phoneNumber + " successfully");
        Toast.makeText(context, "Contact '" + name + "'(" + phoneNumber + ") is added.", Toast.LENGTH_SHORT).show();
    }

    public void removeContact(String name) {
        ContentResolver contentResolver = context.getContentResolver();

        // Define the selection criteria
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?";
        String[] selectionArgs = { name };

        Log.d("ContactHelper", "Remove contact " + name);
        // Query for the contact ID
        Cursor cursor = contentResolver.query(
                //ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.Data.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int data_id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data._ID));
                int raw_contact_id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                Cursor c = context.getContentResolver().query(
                        ContactsContract.RawContacts.CONTENT_URI
                        , new String[] {ContactsContract.RawContacts.ACCOUNT_TYPE,
                                ContactsContract.RawContacts.ACCOUNT_NAME}
                        , ContactsContract.RawContacts._ID+"=?"
                        , new String[] {String.valueOf(raw_contact_id)}
                        , null);

                if(c != null && c.moveToFirst()) {
                    if(!c.isAfterLast()) {
                        String c_type = c.getString(
                                c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                        String c_name = c.getString(
                                c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                        if (c_type.equals(accountType) && c_name.equals(accountName)) {
                            Log.d("ContactHelper", "remove contact " + name + "(id: " + raw_contact_id + ")");

                            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                                    .withSelection(ContactsContract.Data._ID+"=?", new String[] {String.valueOf(data_id)})
                                    .build());
                            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                    .withSelection(ContactsContract.RawContacts._ID+"=?", new String[] {String.valueOf(raw_contact_id)})
                                    .build());
                            try {
                                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                Toast.makeText(context, "Contact '" + name + "'(id: " + raw_contact_id + ") is removed.", Toast.LENGTH_SHORT).show();
                            } catch (OperationApplicationException e) {
                                e.printStackTrace();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                c.close();
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
}
