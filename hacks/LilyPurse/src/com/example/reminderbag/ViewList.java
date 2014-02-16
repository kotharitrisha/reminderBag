package com.example.reminderbag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ViewList extends Activity{
	String type;
	//List<Map<String, String>> list_priority = new ArrayList<Map<String, String>>();
	//List<Map<String, String>> list_ignore = new ArrayList<Map<String, String>>();
	ArrayAdapter<String> adapter;
	ArrayList<String> list_priority = new ArrayList<String>();
	ArrayList<String> list_ignore = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Toast.makeText(getApplicationContext(), "Reached", Toast.LENGTH_SHORT).show();
		setContentView(R.layout.contacts_list_view);
		
		type = getIntent().getCharSequenceExtra("map").toString();
		if (type.equals("priority")) {
			int n = MainActivity.priority_map.keySet().size();
			int i = 0;
			String[] array_p = new String[n];
			for (String key: MainActivity.priority_map.keySet()) {
				array_p[i]= key;
				i++;
			}
			adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, array_p);
		}
		
		if (type.equals("ignore")) {
			Toast.makeText(getApplicationContext(), "IGNORE", Toast.LENGTH_SHORT).show();
			int n = MainActivity.ignore_map.keySet().size();
			int i = 0;
			String[] array_p = new String[n];
			for (String key: MainActivity.ignore_map.keySet()) {
				array_p[i]= key;
				i++;
			}
			adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, array_p);
		}
		
		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setAdapter(adapter);
	}
	
}
