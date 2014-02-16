package com.example.reminderbag;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class CreateActivity extends Activity {
	//public HashMap<String, String> priority_list= new HashMap<String, String>();
	public static final int PICK_CONTACT = 1;
	//public HashMap<String, String> priority_map;
	String type = "";
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_list);
		type = getIntent().getCharSequenceExtra("map").toString();
		
		//getIntent().getExtras();
		//MainActivity.priority_map = (HashMap<String, String>) b.getSerializable("map");
		
		Button add = (Button) findViewById(R.id.add_contact);
		add.setOnClickListener(new View.OnClickListener() {	

			@Override
			public void onClick(View arg0) {
				//priority_list.put("Mark", "2158377900");
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);

			}
		});
		
		Button view = (Button) findViewById(R.id.view_list);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ViewList.class);
				intent.putExtra("map", type);
				startActivity(intent);
			}
		});

		
		/*ListView lv = (ListView) findViewById(R.id.listView1);
		ArrayList<String> list = new ArrayList<String>();
		for (String x: priority_list.keySet()) {
			list.add(x);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, list);
		lv.setAdapter(adapter);
		//*/
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
						Toast.makeText(getApplicationContext(), "Name" + name + cNumber, Toast.LENGTH_LONG).show();

						String[] no_components = cNumber.split(" ");
						for (int i = 0; i < no_components.length; i++) {
							number = number+no_components[i];
						}
						Toast.makeText(getApplicationContext(), "Name" + name + number, Toast.LENGTH_LONG).show();
						if (type.equals("priority"))
							MainActivity.priority_map.put(name, number);	
						if (type.equals("ignore"))
							MainActivity.ignore_map.put(name, number);
					}
					
				}
			}
		}
	}
	
}
