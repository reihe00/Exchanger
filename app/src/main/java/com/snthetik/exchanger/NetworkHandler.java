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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class NetworkHandler extends Thread{
    public static final String EXTRA_MESSAGE_N = "com.snthetik.exchanger.MESSAGEN";
    public static boolean alreadylistening=false;
    public static PublicKey serverKey;
    public static SecretKey aesKey;
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
                System.out.println("sending unencoded message: " + mestoSend);
                outToServer.write(new String(mestoSend + "\n").getBytes("UTF8"));
            }else{
                System.out.println("sending encoded message: " + mestoSend);
                sendEncryptedMessageToServer(mestoSend,outToServer);
            }

            if(!alreadylistening){
                alreadylistening=true;
                stayConnected=true;
                outToServer.write(new String(pass + "\n").getBytes("UTF8"));
                if(MainActivity.useEncryption){
                    String encKey = "#pubkey#" + MainActivity.publicKeyToString(MainActivity.pubKey);
                    encKey = encKey.replaceAll("\n", "#n#") + "\n";
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
                    answer=answer.replaceAll("#n#","\n");
                    decodeKey(answer);
                    answer="Connection is secure!\n";
                }else if(answer.startsWith("#aeskey#")){
                    			//decrypt here
                        System.out.println(answer.length());
                        answer=answer.replaceAll("#aeskey#", "");
                        answer=answer.replaceAll("\n", "");
                        answer=answer.replaceAll("#n#","\n");
                        System.out.println(answer.length());
                        decryptAESKey(hexStringToByteArray(answer));
                        answer="AES-Key received";

                }else if(answer.startsWith("#encoded#")){
                if(serverKey!=null&&MainActivity.useEncryption) {			//decrypt here
                    answer=answer.replaceAll("#encoded#", "");
                    answer=answer.replaceAll("\n", "");
                    answer=answer.replaceAll("#n#","\n");
                    answer = MainActivity.decryptAES(aesKey, answer);
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

    @TargetApi(26)
    public void sendEncryptedMessageToServer(String mes, DataOutputStream outToServer) throws Exception{
        String enc ="#encoded#"+Base64.getEncoder().encodeToString(MainActivity.encryptAES(aesKey,mestoSend));
        enc=enc.replaceAll("\n","#n#");
        enc+="\n";
        outToServer.write(enc.getBytes(("UTF8")));
        if(mes.startsWith("#disconnect#")){
            serverKey=null;
            aesKey=null;
        }
    }

    @TargetApi(26)
    private void decodeKey(String answer){
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");


            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(answer));
            serverKey = kf.generatePublic(keySpecX509);
            System.out.println("Serverkey= " + MainActivity.publicKeyToString(serverKey));
            System.out.println("Clientkey= " + MainActivity.publicKeyToString(MainActivity.pubKey));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void decryptAESKey(byte[] aeskeyencrypted){

        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.PRIVATE_KEY, MainActivity.privKey);
            System.out.println(new String(aeskeyencrypted));
            System.out.println(aeskeyencrypted.length);
            byte[] decryptedKey = cipher.doFinal(aeskeyencrypted);
            SecretKey originalKey = new SecretKeySpec(decryptedKey , 0, decryptedKey .length, "AES");
            aesKey = originalKey;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
