package com.example.personalizedtext;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

public class ContactListActivity extends ListActivity {
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
       String[] from = new String[] {ContactsContract.Contacts.DISPLAY_NAME,ContactsContract.Contacts.HAS_PHONE_NUMBER,ContactsContract.Contacts._ID};
     //  int[] to = new int[] {R.id.checkBox};
       System.out.println("I have reached here in ContactListActivity");
       
       Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null); 
;
     //  ContactListCursorAdapter adapter = new ContactListCursorAdapter(getApplicationContext(), R.layout.listview,  cursor, from, to);
       //setListAdapter(adapter);
    }

}
