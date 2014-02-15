package com.example.reminderbag;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private final static int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
	boolean call_happening_now = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		CallStateListener callStateListener = new CallStateListener(); 
		tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		/*	if (call_happening_now) {
				Toast.makeText(getApplicationContext(), "Call happening", Toast.LENGTH_LONG).show();
			Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
			    // Loop through paired devices
			    for (BluetoothDevice device : pairedDevices) {
			        // Add the name and address to an array adapter to show in a ListView
			    	if (device.getName().equals("RNBT-2634")) {
			    		Log.d("DEBUG", "Trisha detected..");
			    		
			    		ConnectThread connectThread = new ConnectThread(device);
			    		connectThread.run();
			    	}
			    	
			    	Log.d("DEBUG", "Found " + device.getName() + "\n" + device.getAddress());
			    }
			}	
			}*/
		/*(try {
			Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
			ParcelUuid[] uuids =  (ParcelUuid[]) getUuidsMethod.invoke(bta, null); 
			for (ParcelUuid uuid: uuids) {
				Log.d("DEBUG", "UUID: " + uuid.getUuid().toString());
			}
			
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View arg0) {
				Log.d("DEBUG", "Clicked!");
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        bta = BluetoothAdapter.getDefaultAdapter();
	    	BluetoothSocket tmp = null;
	        mmDevice = device;
	        Log.d("DEBUG", "connect thread initialized");
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            //final UUID MY_UUID = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");
	        	final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        	
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	    	Log.d("DEBUG", "Run has been called");
	        // Cancel discovery because it will slow down the connection
	        bta.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            Log.d("DEBUG", "Are we connecting");
	        	mmSocket.connect();
	            Log.d("DEBUG", "Msocket connected in connect thread");
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	        	Log.d("DEBUG", "Woah Exception caught");
	        	connectException.printStackTrace();
	            try {
	                mmSocket.close();
		        	Log.d("DEBUG", "Woahhhh Exception caught");

	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        // manageConnectedSocket(mmSocket);
	        
	        ConnectedThread ct = new ConnectedThread(mmSocket);
	        byte[] buffer = new byte[1024];
	        buffer[0] = 'a';
	        ct.write(buffer);
	        Log.d("DEBUG", "WHat the hell is going on");
	    }
	 

		/** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                //bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                  //      .sendToTarget();
	            } catch (Exception e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	        	Log.d("DEBUG", "Writing heyy");
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	/**
	* Listener to detect incoming calls. 
	*/
	private class CallStateListener extends PhoneStateListener {
	  @Override
	  public void onCallStateChanged(int state, String incomingNumber) {
	      switch (state) {
	          case TelephonyManager.CALL_STATE_RINGING:
	          // called when someone is ringing to this phone
	        	  
	          Toast.makeText(getApplicationContext(), 
	                  "Look who is calling: "+incomingNumber, 
	                  Toast.LENGTH_LONG).show();
	          call_happening_now = true;
	          if (call_happening_now) {
					Toast.makeText(getApplicationContext(), "Call happening", Toast.LENGTH_LONG).show();
				Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
				// If there are paired devices
				if (pairedDevices.size() > 0) {
				    // Loop through paired devices
				    for (BluetoothDevice device : pairedDevices) {
				        // Add the name and address to an array adapter to show in a ListView
				    	if (device.getName().equals("RNBT-2634")) {
				    		Log.d("DEBUG", "Trisha detected..");
				    		
				    		ConnectThread connectThread = new ConnectThread(device);
				    		connectThread.run();
				    	}
				    	
				    	Log.d("DEBUG", "Found " + device.getName() + "\n" + device.getAddress());
				    }
				}	
	          
	          break;
	          }	
	      }
	  }
	}
	
}