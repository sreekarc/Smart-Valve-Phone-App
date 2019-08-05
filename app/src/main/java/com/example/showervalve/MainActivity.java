package com.example.showervalve;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    boolean valveControl = true;
    ToggleButton toggle;
    public TextView settings;
    public TextView flowView;
    public Socket socket;
    public static DataOutputStream out;
    public boolean thread2 = false;
    public boolean flowing = false;
    public String message;
    public Handler updateUIHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //somehow have to get valve info DONT FORGET!!!

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        toggle = (ToggleButton) findViewById(R.id.toggleButton);

        flowView = (TextView) findViewById(R.id.textView2);

        settings = (TextView) findViewById(R.id.textView);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Pop.class));
            }
        });

        new Thread(new ClientThread()).start();
        new Thread(new FlowThread()).start();

        createUpdateUiHandler();
    }

    public void onClick(View view) {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            if(toggle.isChecked()){
                Log.d("Toggle", "Button is on");
                valveControl = true;
                out.writeUTF("on");
            }
            else{
                Log.d("Toggle", "Button is off");
                valveControl = false;
                out.writeUTF("off");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("192.168.0.72");
                socket = new Socket(serverAddr, 3000);
                Log.d("flow", "thread1");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                message = "";
                int charsRead = 0;
                char[] buffer = new char[2048];
                charsRead = in.read(buffer);
                message += new String(buffer).substring(0, charsRead);
                Message message = new Message();
                message.what = 2;
                updateUIHandler.sendMessage(message);

                thread2 = true;
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void updateFlowText() {
        if(Float.parseFloat(message) == 0.0) {
            flowView.setText("Water is NOT Flowing");
            flowing = false;
        }
        else if(Float.parseFloat(message) > 0.0){
            flowView.setText("Water is Flowing");
            flowing = true;
        }
    }

    private void updateValveState(){
        if(message.equals("on"))
            toggle.setChecked(true);
        else
            toggle.setChecked(false);
    }

    private void createUpdateUiHandler() {
        if(updateUIHandler == null) {
            updateUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == 1) {
                        updateFlowText();
                    }
                    else if(msg.what == 2){
                        updateValveState();
                    }
                }
            };
        }
    }

    class FlowThread implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    Log.d("flow", "thread2" + thread2);
                    if (thread2 == true) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                        message = "";
                        int charsRead = 0;
                        char[] buffer = new char[2048];


                        charsRead = in.read(buffer);
                        message += new String(buffer).substring(0, charsRead);

                        Log.d("flow", "left buffer");

                        Message message = new Message();
                        message.what = 1;
                        updateUIHandler.sendMessage(message);
                    }
                    Thread.sleep(1000);
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}