package com.nimo.mori.server;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ServerActivity";
    private static final int PORT = 8080;

    ServerSocketThread mServerSocketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.restart);
        btn.setOnClickListener(this);

        TextView textView = findViewById(R.id.message);
        EditText editPort = findViewById(R.id.port);
        editPort.setText(PORT);
        mServerSocketThread = new ServerSocketThread(textView, PORT);
        mServerSocketThread.start();
    }

    @Override
    public void onClick(View v) {
        //mServerSocketThread
    }

     public class ServerSocketThread extends Thread {
        private TextView mTextView;
        private int mPort;

        ServerSocketThread(TextView view, int port) {
            mTextView = view;
            mPort = port;
        }

        private void print(final String msg) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String text = msg + '\n' + mTextView.getText();
                    mTextView.setText(text);
                }
            });
        }

         private String getIpAddress() {
             String ip = "";
             try {
                 Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                         .getNetworkInterfaces();
                 while (enumNetworkInterfaces.hasMoreElements()) {
                     NetworkInterface networkInterface = enumNetworkInterfaces
                             .nextElement();
                     Enumeration<InetAddress> enumInetAddress = networkInterface
                             .getInetAddresses();
                     while (enumInetAddress.hasMoreElements()) {
                         InetAddress inetAddress = enumInetAddress.nextElement();

                         if (inetAddress.isSiteLocalAddress()) {
                             ip += "SiteLocalAddress: "
                                     + inetAddress.getHostAddress() + "\n";
                         }
                     }
                 }
             } catch (SocketException e) {
                 e.printStackTrace();
                 ip += "Something Wrong! " + e.toString() + "\n";
             }
             return ip;
         }

        @Override
        public void run() {
            print(getIpAddress());

            try (
                    ServerSocket server = new ServerSocket(mPort);
            ) {
                while (true) {
                    print("Waiting for client request from " + mPort + " port");
                    try (
                            Socket socket = server.accept();
                            PrintWriter out =
                                    new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream()));
                    ) {
                        String inputLine;
                        print("Connected.");
                        out.println("Welcome!");

                        while ((inputLine = in.readLine()) != null) {
                            out.println(inputLine);
                            print("Client: " + inputLine);
                            if (inputLine.equals("Bye."))
                                break;
                        }
                    } catch (IOException e) {
                        print("IOException.");
                        Log.d(TAG, "IOException!", e);
                    }
                }
            } catch (IOException e) {
                print("IOException.");
                Log.d(TAG, "IOException!", e);
            } catch (InterruptedException e) {
                Log.d(TAG, "Stopping the server");
            }
        }
    }
}
