package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.example.administrator.PickingStation.Commands.PING;

public class TcpClient {

    Socket socket=null;
    InetAddress SERVER_IP;
    int SERVER_PORT;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;

    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    public static int timeOuts = 0;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void sendMessage(String message) {
        Log.d("TCP Client", "C: Sending...");
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    public void run(Context mContext) {


        mRun = true;

        //Connect to server on first run
        connectToServer(mContext);

        //While connection is required
        while (mRun) {

            //Attempt read
            try{
                mServerMessage = mBufferIn.readLine();
                if (mServerMessage != null && mMessageListener != null) {
                    mMessageListener.messageReceived(mServerMessage);
                    Log.d("CMD received", mServerMessage);
                }
                else{
                    Log.d("CMD received","false");
                    closeSocket();
                    connectToServer(mContext);
                }

            //Timeout Exception
            } catch(SocketTimeoutException timeOutEx){
                sendMessage(PING); //Are you still there? ._.
                timeOuts++;
                if(timeOuts > 3) {
                    //Connection lost, Reconnect.
                    closeSocket();
                    connectToServer(mContext);
                }

            //Any other exception
            } catch(Exception e) {
                Log.e("TCP Error", "S: Error", e);

            }
        }
        closeSocket();
    }


    private void connectToServer(Context mContext){
        Boolean Errors=true;
        while(Errors){
            try {
                Errors=false;

                //get IP and Port from preferences
                try {
                    SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SERVER_IP = InetAddress.getByName(sharedPref.getString(mContext.getResources().getString(R.string.Stored_PC_IP_Address),"192.168.0.0"));
                    SERVER_PORT = Integer.parseInt(sharedPref.getString(mContext.getResources().getString(R.string.Stored_PC_IP_Port),"24601"));
                } catch (Exception e) {
                    Log.e("String to IP exc.", e.toString());
                }

                //Create new socket and set timeout to 1000m
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT),1000);
                socket.setSoTimeout(1000);
                Log.e("TCP Client", "C: socket has been connected.");

                mMessageListener.connectionEstablished();

            } catch(Exception e){
                Errors=true;

                Log.e("TCP connection failed", "S: Error", e);
                closeSocket();

                System.gc(); //collect garbage
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException sleepExc) {
                    sleepExc.printStackTrace();
                }
            }
        }
        try{
            timeOuts=0;
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         }catch(Exception e) {
        }
    }

    public static void ack(){
        timeOuts = 0;
    }

    private void closeSocket(){
        mMessageListener.connectionLost();
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e("closeSocket(): ", e.toString());
            }
        }
        socket = null;
    }


    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        mMessageListener.connectionLost();

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
        void connectionEstablished();
        void connectionLost();
    }

}