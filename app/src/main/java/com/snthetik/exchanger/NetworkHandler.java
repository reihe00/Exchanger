package com.snthetik.exchanger;

import android.annotation.TargetApi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class NetworkHandler extends Thread{
    public static final String EXTRA_MESSAGE_N = "com.snthetik.exchanger.MESSAGEN";
    public static boolean alreadylistening=false;
    public static PublicKey serverKey;
    public DisplayMessageActivity whocalledme;
    public String mestoSend;
    public String pass;
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
            if(serverKey==null||!MainActivity.useEncryption) {
                outToServer.write(new String(mestoSend + "\n").getBytes("UTF8"));
            }else{

                String enc ="#encoded#"+new String(MainActivity.encrypt(serverKey,mestoSend),"UTF8");
                enc=enc.replaceAll("\n","#n#");
                enc+="\n";
                outToServer.write(enc.getBytes(("UTF8")));
            }

            if(!alreadylistening){
                alreadylistening=true;
                stayConnected=true;
                outToServer.write(new String(pass + "\n").getBytes("UTF8"));
                if(MainActivity.useEncryption){
                    String encKey = "#pubkey#" + MainActivity.publicKeyToString(MainActivity.pubKey) + "\n";
                    outToServer.write(encKey.getBytes("UTF8"));
                }
            }else{

                return;
            }
            while(stayConnected){


                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF8"));
                //System.out.println(clientSocket.isConnected());
                //sentence = inFromUser.readLine();


                answer = inFromServer.readLine();
                System.out.println("antwort erhalten: " + answer);
                if(answer==null){stayConnected=false;alreadylistening=false;}
                if(answer.startsWith("#pubkey#")){
                    answer=answer.replaceAll("#pubkey#","");
                    answer=answer.replaceAll("\n","");
                    decodeKey(answer);
                    answer="Connection is secure!\n";
                }else if(answer.startsWith("#encoded#")){
                    if(serverKey!=null&&MainActivity.useEncryption) {			//decrypt here
                        answer=answer.replaceAll("#encoded#", "");
                        answer=answer.replaceAll("\n", "");
                        answer=answer.replaceAll("#n#","\n");
                        answer = new String(MainActivity.decrypt(MainActivity.privKey, answer.getBytes()));
                    }
                }
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
        outToServer.write(new String(mes + "\n").getBytes("UTF8"));
        System.out.println("daten gesendet");
    }

    @TargetApi(26)
    private void decodeKey(String answer){
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");


            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(answer));
            serverKey = kf.generatePublic(keySpecX509);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
