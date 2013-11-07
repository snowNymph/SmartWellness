/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bluetoothHDP;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHealthAppConfiguration;
import android.bluetooth.BluetoothHealthCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.example.smartwellness.ToKeepVar;

/**
 * This Service encapsulates Bluetooth Health API to establish, manage, and disconnect
 * communication between the Android device and a Bluetooth HDP-enabled device.  Possible HDP
 * device type includes blood pressure monitor, glucose meter, thermometer, etc.
 *
 * As outlined in the
 * <a href="http://developer.android.com/reference/android/bluetooth/BluetoothHealth.html">BluetoothHealth</a>
 * documentation, the steps involve:
 * 1. Get a reference to the BluetoothHealth proxy object.
 * 2. Create a BluetoothHealth callback and register an application configuration that acts as a
 *    Health SINK.
 * 3. Establish connection to a health device.  Some devices will initiate the connection.  It is
 *    unnecessary to carry out this step for those devices.
 * 4. When connected successfully, read / write to the health device using the file descriptor.
 *    The received data needs to be interpreted using a health manager which implements the
 *    IEEE 11073-xxxxx specifications.
 * 5. When done, close the health channel and unregister the application.  The channel will
 *    also close when there is extended inactivity.
 */
public class BluetoothHDPService extends Service {
    private static final String TAG = "BluetoothHDPService";

    public static final int RESULT_OK = 0;
    public static final int RESULT_FAIL = -1;

    // Status codes sent back to the UI client.
    // Application registration complete.
    public static final int STATUS_HEALTH_APP_REG = 100;
    // Application unregistration complete.
    public static final int STATUS_HEALTH_APP_UNREG = 101;
    // Channel creation complete.
    public static final int STATUS_CREATE_CHANNEL = 102;
    // Channel destroy complete.
    public static final int STATUS_DESTROY_CHANNEL = 103;
    // Reading data from Bluetooth HDP device.
    public static final int STATUS_READ_DATA = 104;
    // Done with reading data.
    public static final int STATUS_READ_DATA_DONE = 105;

    // Message codes received from the UI client.
    // Register client with this service.
    public static final int MSG_REG_CLIENT = 200;
    // Unregister client from this service.
    public static final int MSG_UNREG_CLIENT = 201;
    // Register health application.
    public static final int MSG_REG_HEALTH_APP = 300;
    // Unregister health application.
    public static final int MSG_UNREG_HEALTH_APP = 301;
    // Connect channel.
    public static final int MSG_CONNECT_CHANNEL = 400;
    // Disconnect channel.
    public static final int MSG_DISCONNECT_CHANNEL = 401;

    private BluetoothHealthAppConfiguration mHealthAppConfig;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHealth mBluetoothHealth;
    private BluetoothDevice mDevice;
    private int mChannelId;

    private byte[] invoke = {0x00, 0x00};

    private int count;
    private Messenger mClient;
    public static ArrayList<String> mParseData_weight = new ArrayList<String>();
    public static ArrayList<String> mParseData_blood_pressure = new ArrayList<String>();
    
    public ArrayList<byte[]> qwer;
    //private FileWriter debugFile;
    //LogConfigurator logcon;

    // Handles events sent by {@link HealthHDPActivity}.
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Register UI client to this service so the client can receive messages.
                case MSG_REG_CLIENT:
                    Log.d(TAG, "Activity client registered");
                    mClient = msg.replyTo;
                    break;
                // Unregister UI client from this service.
                case MSG_UNREG_CLIENT:
                    mClient = null;
                    break;
                // Register health application.
                case MSG_REG_HEALTH_APP:
                    registerApp(msg.arg1);
                    break;
                // Unregister health application.
                case MSG_UNREG_HEALTH_APP:
                    unregisterApp();
                    break;
                // Connect channel.
                case MSG_CONNECT_CHANNEL:
                    mDevice = (BluetoothDevice) msg.obj;
                    connectChannel();
                    break;
                // Disconnect channel.
                case MSG_DISCONNECT_CHANNEL:
                    mDevice = (BluetoothDevice) msg.obj;
                    disconnectChannel();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Make sure Bluetooth and health profile are available on the Android device.  Stop service
     * if they are not available.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if(mParseData_blood_pressure != null)
	        mParseData_blood_pressure.clear();
        if(mParseData_weight != null)
        	mParseData_weight.clear();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth adapter isn't available.  The client of the service is supposed to
            // verify that it is available and activate before invoking this service.
            stopSelf();
            return;
        }
        if (!mBluetoothAdapter.getProfileProxy(this, mBluetoothServiceListener,
                BluetoothProfile.HEALTH)) {
            //Toast.makeText(this, R.string.bluetooth_health_profile_not_available, Toast.LENGTH_LONG);
            stopSelf();
            return;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothHDPService is running.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    };

    // Register health application through the Bluetooth Health API.
    private void registerApp(int dataType) {
        mBluetoothHealth.registerSinkAppConfiguration(TAG, dataType, mHealthCallback);
    }

    // Unregister health application through the Bluetooth Health API.
    private void unregisterApp() {
        mBluetoothHealth.unregisterAppConfiguration(mHealthAppConfig);
    }

    // Connect channel through the Bluetooth Health API.
    private void connectChannel() {
        Log.i(TAG, "connectChannel()");
        mBluetoothHealth.connectChannelToSource(mDevice, mHealthAppConfig);
    }

    // Disconnect channel through the Bluetooth Health API.
    private void disconnectChannel() {
        Log.i(TAG, "disconnectChannel()");
        mBluetoothHealth.disconnectChannel(mDevice, mHealthAppConfig, mChannelId);
    }

    // Callbacks to handle connection set up and disconnection clean up.
    private final BluetoothProfile.ServiceListener mBluetoothServiceListener =
            new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = (BluetoothHealth) proxy;
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onServiceConnected to profile: " + profile);
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = null;
            }
        }
    };

    private final BluetoothHealthCallback mHealthCallback = new BluetoothHealthCallback() {
        // Callback to handle application registration and unregistration events.  The service
        // passes the status back to the UI client.
        public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config,
                int status) {
            if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_FAILURE) {
                mHealthAppConfig = null;
                sendMessage(STATUS_HEALTH_APP_REG, RESULT_FAIL);
            } else if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_SUCCESS) {
                mHealthAppConfig = config;
                sendMessage(STATUS_HEALTH_APP_REG, RESULT_OK);
            } else if (status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_FAILURE ||
                    status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_SUCCESS) {
                sendMessage(STATUS_HEALTH_APP_UNREG,
                        status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_SUCCESS ?
                        RESULT_OK : RESULT_FAIL);
            }
        }

        // Callback to handle channel connection state changes.
        // Note that the logic of the state machine may need to be modified based on the HDP device.
        // When the HDP device is connected, the received file descriptor is passed to the
        // ReadThread to read the content.
        public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config,
                BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd,
                int channelId) {
            //if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, String.format("prevState\t%d ----------> newState\t%d", prevState, newState));
            if ((prevState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED &&
                    newState == BluetoothHealth.STATE_CHANNEL_CONNECTED) || (prevState == BluetoothHealth.STATE_CHANNEL_CONNECTING && newState == BluetoothHealth.STATE_CHANNEL_CONNECTED)) {
                if (config.equals(mHealthAppConfig)) {
                    mChannelId = channelId;
                    sendMessage(STATUS_CREATE_CHANNEL, RESULT_OK);
                    (new ReadThread(fd)).start();
                } else {
                    sendMessage(STATUS_CREATE_CHANNEL, RESULT_FAIL);
                }
            } else if (prevState == BluetoothHealth.STATE_CHANNEL_CONNECTING &&
                       newState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED) {
                sendMessage(STATUS_CREATE_CHANNEL, RESULT_FAIL);
            } else if (newState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED) {
                if (config.equals(mHealthAppConfig)) {
                    sendMessage(STATUS_DESTROY_CHANNEL, RESULT_OK);
                } else {
                    sendMessage(STATUS_DESTROY_CHANNEL, RESULT_FAIL);
                }
            }
        }
    };

    // Sends an update message to registered UI client.
    private void sendMessage(int what, int value) {
        if (mClient == null) {
            Log.d(TAG, "No clients registered.");
            return;
        }

        try {
            mClient.send(Message.obtain(null, what, value, 0));
        } catch (RemoteException e) {
            // Unable to reach client.
            e.printStackTrace();
        }
    }

    // Thread to read incoming data received from the HDP device.  This sample application merely
    // reads the raw byte from the incoming file descriptor.  The data should be interpreted using
    // a health manager which implements the IEEE 11073-xxxxx specifications.
    private class ReadThread extends Thread {
        private ParcelFileDescriptor mFd;

        public ReadThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            FileInputStream fis = new FileInputStream(mFd.getFileDescriptor());
            
            final byte data[] = new byte[1000];
            try {
                while(fis.read(data) > -1) {
                	//qwer.add(data);
                    // At this point, the application can pass the raw data to a parser that
                    // has implemented the IEEE 11073-xxxxx specifications.  Instead, this sample
                    // simply indicates that some data has been received.
                    if (data[0] != (byte) 0x00)
                    {
                    	String test = byte2hex(data);
                        writeLog(test);
                        //Log.i(TAG, test);

                        if(data[0] == (byte) 0xE2){
                            Log.i(TAG, "E2");
                            count = 1;

                            (new WriteThread(mFd)).start();
                            try {
								sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                            Log.i(TAG, "Seconds Write Thread");
                            count = 2;
                            (new WriteThread(mFd)).start();
                        }
                        else if (data[0] == (byte)0xE7){                        
                            count = 3;
                            //set invoke id so get correct response
                            invoke = new byte[] { data[6], data[7] };
                            (new WriteThread(mFd)).start();

                            if(ToKeepVar.getInstance().getType() == 0x100F){
								int exponent = data[40]&0xFF;
								float a = (float)((data[41]&0xFF * 65536) + (data[42]&0xFF) * 256 + (data[43]&0xFF));
								float b = (float)Math.pow(10, 256 - exponent);
								double weight = a/b;
								Log.i(TAG, String.format("weight=%f", weight));
								mParseData_weight.add(Double.toString(weight));
                            }

                            //parse data!!
                            if(ToKeepVar.getInstance().getType() == 0x1007){
	                            int number_of_data_packets = data[25];
	                            int packet_start = 30;
	                            final int SYS_DIA_MAP_DATA = 1;
	                            final int PULSE_DATA = 2;
	                            final int ERROR_CODE_DATA = 3;
	
	                            for (int i = 0; i < number_of_data_packets; i++){
	                                   Log.e("TEST", Integer.toString(i));
	                                  int obj_handle = data[packet_start+1];
	                                  switch (obj_handle)
	                                  {
	                                  case SYS_DIA_MAP_DATA:
	                                      int sys = byteToUnsignedInt(data[packet_start+9]);
	                                      int dia = byteToUnsignedInt(data[packet_start+11]);
	                                      int map = byteToUnsignedInt(data[packet_start+13]);
	                                      //create team string... 9+13~9+20   
	                                      Log.e("RESULT", "sys is "+ sys);
	                                      //sendMessage(RECEIVED_SYS, sys);
	                                      Log.e("RESULT", "dia is "+ dia);
	                                      //sendMessage(RECEIVED_DIA, dia);
	                                      Log.e("RESULT", "map is "+ map);
	                                      mParseData_blood_pressure.add(Integer.toString(sys));
	                                      mParseData_blood_pressure.add(Integer.toString(dia));
	                                      break;
	                                  case PULSE_DATA:
	                                      //parse
	                                      int pulse = byteToUnsignedInt(data[packet_start+10]);
	                                      Log.e("RESULT", "pulse is " + pulse);
	                                      //sendMessage(RECEIVED_PUL, pulse);
	                                      break;
	                                  case ERROR_CODE_DATA:
	                                      //need more signal
	                                      break;
	                                  }
	                                  packet_start += 1;//4 + data[packet_start+3];   //4 = ignore beginning four bytes
	                             }
                            }
                        }
                        else if (data[0] == (byte) 0xE4)
                        {
                            count = 4;
                            (new WriteThread(mFd)).start();
                        }
                        else if (data[0] == (byte) 0xE6 )
                        {
                        	//Abort
                        	count = 5;
                        	(new WriteThread(mFd)).start();
                        }
                        //zero out the data
                        for (int i = 0; i < data.length; i++){
                            data[i] = (byte) 0x00;
                        }
                    }
                    sendMessage(STATUS_READ_DATA, 0);
                }
            } catch(IOException ioe) {}
            if (mFd != null) {
                try {
                    mFd.close();
                } catch (IOException e) { }
            }
            sendMessage(STATUS_READ_DATA_DONE, 0);
        }
    }
    
    void writeLog(String str) {
    	  String str_Path_Full = Environment.getExternalStorageDirectory()
    	    .getAbsolutePath() + "/Alarms/log.txt";
    	  File file = new File(str_Path_Full);
    	  if (file.exists() == false) {
    	   try {
    	    file.createNewFile();
    	   } catch (IOException e) {
    	   }
    	  } else {
    	   try {
    	    BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full,true));  
    	    bfw.write(str);
    	    bfw.write("\n");
    	    bfw.flush();
    	    bfw.close();
    	   } catch (FileNotFoundException e) {
    	   } catch (IOException e) {
    	   }
    	  }
    	 }

    public void appendLog(String text)
    {       
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
              log.append(line);
            }
          } catch (IOException e) {
          }
	}

    public String byte2hex(byte bytes[]){ 
        StringBuffer sb = new StringBuffer(); 
        for(int i = 0; i < bytes.length; ++i){ 
            sb.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1)); 
        } 
        return sb.toString(); 
    } 
    
    public int byteToUnsignedInt(byte b) {
        return 0x00 << 24 | b & 0xff;
	}
    
	private class WriteThread extends Thread {
	    private ParcelFileDescriptor mFd;
	
	    public WriteThread(ParcelFileDescriptor fd) {
	        super();
	        mFd = fd;
	    }
	
	    @Override
	    public void run() {
	        FileOutputStream fos = new FileOutputStream(mFd.getFileDescriptor());
	        final byte data_AR[] = new byte[] {         (byte) 0xE3, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x2C, 
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x50, (byte) 0x79,
	                                                    (byte) 0x00, (byte) 0x26,
	                                                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x80, (byte) 0x00,
	                                                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x08,  //bt add for phone, can be automate in the future
	                                                    /*(byte) 0x00, (byte) 0x09, (byte) 0x1F, (byte) 0xFF, 
                                                        (byte) 0xFE, (byte) 0x80, (byte) 0x18, (byte) 0xCC,*/
	                                                    (byte) 0x3C, (byte) 0x5A, (byte) 0x37, (byte) 0xFF, 
                                                        (byte) 0xFE, (byte) 0x95, (byte) 0xEE, (byte) 0xE3,
	                                                    /*(byte) 0x36, (byte) 0x37, (byte) 0x50, (byte) 0x42, 
	                                                    (byte) 0x54, (byte) 0x2D, (byte) 0x43, (byte) 0x20,*/
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00, 
	                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	        final byte data_DR[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x12,
	                                                    (byte) 0x00, (byte) 0x10,
	                                                    (byte) invoke[0], (byte) invoke[1],
	                                                    (byte) 0x02, (byte) 0x01,
	                                                    (byte) 0x00, (byte) 0x0A,
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x0D, (byte) 0x1D,
	                                                    (byte) 0x00, (byte) 0x00 };
	
	        final byte get_MDS[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x0E,
	                                                    (byte) 0x00, (byte) 0x0C,
	                                                    (byte) 0x00, (byte) 0x24,
	                                                    (byte) 0x01, (byte) 0x03,
	                                                    (byte) 0x00, (byte) 0x06,
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x00 };
	
	        final byte data_RR[] = new byte[] {         (byte) 0xE5, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x02,
	                                                    (byte) 0x00, (byte) 0x00 };
	
	        final byte data_RRQ[] = new byte[] {        (byte) 0xE4, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x02,
	                                                    (byte) 0x00, (byte) 0x00 };
	
	        final byte data_ABORT[] = new byte[] {      (byte) 0xE6, (byte) 0x00,
	                                                    (byte) 0x00, (byte) 0x02,
	                                                    (byte) 0x00, (byte) 0x00 };
	        
	        
	        try {
	            Log.i(TAG, String.valueOf(count));
	            if (count == 1)
	            {
	                fos.write(data_AR);
	                Log.i(TAG, "Association Responded!");
	            }  
	            else if (count == 2)
	            {
	                fos.write(get_MDS);
	                Log.i(TAG, "Get MDS object attributes!");
	            }
	            else if (count == 3) 
	            {
	                fos.write(data_DR);
	                Log.i(TAG, "Data Responsed!");
	            }
	            else if (count == 4)
	            {
	                fos.write(data_RR);
	                Log.i(TAG, "Data Released!");
	            }
	            else if( count == 5)
	            {
	            	fos.write(data_ABORT);
	            	Log.i(TAG, "data abort");
	            	fos.close();
	            }
	        } catch(IOException ioe) {}
	    }
	}
}
