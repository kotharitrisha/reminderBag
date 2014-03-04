package com.example.personalizedtext;


import com.example.*;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	String newText="";
	String textMessage = "";
	String name="";
	final static int PICK_CONTACT = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
        
        
        final Button continueToContacts = 
        			(Button) findViewById(R.id.continueToContacts);
        continueToContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	final EditText editText = (EditText) findViewById(R.id.textMessage);
            	textMessage = editText.getText().toString();
            	System.out.println("Your new text message is " + textMessage); 
            	
            	//Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            	//Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Intents);

            	Intent intent = new Intent(getApplicationContext(), ChooseContacts.class);
            	//startActivity(new Intent (this, ChooseContacts.class));
            	startActivity(intent);
            	//;

            	
            	//Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
            	//startActivity(intent);
            	/*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
                */      
            }
        });
    }

    
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{ 
                                ContactsContract.CommonDataKinds.Phone.NUMBER,  
                                ContactsContract.CommonDataKinds.Phone.TYPE, 
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                    		},
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                        String[] all_name_words = name.split(" ");
                        name = all_name_words[0];
                        newText = textMessage.replaceAll("@name", "Fatty");
                        System.out.println("Name found is "+ name);
                        showSelectedNumber(type, number);
                    }
                } finally {
                    if (c != null) {
                        //c.close();
                    }
                }
            }
        }
    }

    public void showSelectedNumber(int type, String number) {
        Toast.makeText(this, type + ": " + number, Toast.LENGTH_LONG).show();      
        //SmsManager smsManager = SmsManager.getDefault();
    	//smsManager.sendTextMessage("+919820910120", null, newText, null, null);  
    }
    
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT) :
			if (resultCode == Activity.RESULT_OK) {
				String cNumber = "";
				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					
					if (hasPhone.equalsIgnoreCase("1")) {
						
						Cursor phones = getContentResolver().query( 
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
								null, null);
						phones.moveToFirst();

						cNumber = phones.getString(phones.getColumnIndex("data1"));
						System.out.println("number is:"+cNumber);
						String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						Log.d("DEBUG", "My chosen name is " + name);
						Log.d("DEBUG", "My chosen no is " + cNumber);
						String number = "";
						//Toast.makeText(getApplicationContext(), "Name" + name + cNumber, Toast.LENGTH_LONG).show();

						String[] no_components = cNumber.split(" ");
						for (int i = 0; i < no_components.length; i++) {
							number = number+no_components[i];
						}
//						Toast.makeText(getApplicationContext(), "Name" + name + number, Toast.LENGTH_LONG).show();
						 String[] all_name_words = name.split(" ");
	                     name = all_name_words[0];
						newText = textMessage.replaceAll("@name", name);
						Toast.makeText(getApplicationContext(), newText + " " + number , Toast.LENGTH_LONG).show();
						
						//SmsManager smsManager = SmsManager.getDefault();
				    	//smsManager.sendTextMessage(number, null, newText, null, null); 
					
					}
				}
			}

		}
    }
}
