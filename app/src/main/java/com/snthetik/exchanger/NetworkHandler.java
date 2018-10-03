package com.snthetik.exchanger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.net.Socket;

public class NetworkHandler extends Thread{
    public static final String EXTRA_MESSAGE_N = "com.snthetik.exchanger.MESSAGEN";
    public static boolean alreadylistening=false;
    public DisplayMessageActivity whocalledme;
    public String mestoSend;
    public String url;
    public boolean stayConnected=false;
    private static Socket clientSocket;
    @Override
    public void run(){
        try {
            System.out.println("Inside : " + Thread.currentThread().getName());
            System.out.println("connecting");
            if(clientSocket==null||!alreadylistening)
            clientSocket = new Socket(url, 21245);
            else System.out.println("nicht die erste nachricht");
            String answer = "";
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(mestoSend + "\n");
            if(!alreadylistening){
                alreadylistening=true;
                stayConnected=true;
            }else{

                return;
            }
            while(stayConnected){


                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println(clientSocket.isConnected());
                //sentence = inFromUser.readLine();


                answer = inFromServer.readLine();
                System.out.println("antwort erhalten: " + answer);
                if(answer==null){stayConnected=false;alreadylistening=false;}
                whocalledme.HeyImFinished(answer);
            }


            clientSocket.close();
        }catch (Exception e){
            System.out.println(e.toString());
            whocalledme.HeyImFinished(e.toString());
        }
    }


    public void sendMessageToServer(String mes) throws Exception{
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(mes + "\n");
        System.out.println("daten gesendet");
    }
}
