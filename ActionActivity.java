package com.jonathan.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ActionActivity extends AppCompatActivity {
    Button ledOnButton;
    TextView receivedText;
    View block1;
    View block2;


    private static String address;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    Handler bluetoothIn;
    final int handlerState = 0;

    private ConnectedThread mConnectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        ledOnButton = (Button) findViewById(R.id.ledOnButton);

        receivedText = (TextView) findViewById(R.id.receivedText);

        block1 = (View) findViewById(R.id.colorBlock1);
        block2 = (View) findViewById(R.id.colorBlock2);

        bluetoothIn = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == handlerState){
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");
                    if(endOfLineIndex > 0){
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        receivedText.setText("Data Received = " + dataInPrint);
                        recDataString.delete(0, recDataString.length());
                        switch (dataInPrint){
                            case "0":
                                block2.setBackgroundColor(Color.parseColor("#FF0000"));
                                block1.setBackgroundColor(Color.parseColor("#FF0000"));
                                break;
                            case "1":
                                block2.setBackgroundColor(Color.parseColor("#0000FF"));
                                block1.setBackgroundColor(Color.parseColor("#FF0000"));
                                break;
                            case "2":
                                block1.setBackgroundColor(Color.parseColor("#0000FF"));
                                block2.setBackgroundColor(Color.parseColor("#FF0000"));
                                break;
                            case "3":
                                block2.setBackgroundColor(Color.parseColor("#0000FF"));
                                block1.setBackgroundColor(Color.parseColor("#0000FF"));
                                break;
                            default:
                                break;
                        }

                        dataInPrint = " ";
                }

                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        ledOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectedThread.write("1");
                ledOnButton.setText("OFF");
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        mConnectedThread.write("x");
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }
}
