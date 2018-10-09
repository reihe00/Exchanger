package com.snthetik.exchanger;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.snthetik.exchanger.MESSAGE";
    public static final String EXTRA_URL = "com.snthetik.exchanger.URL";
    public static final String EXTRA_PASS = "com.snthetik.exchanger.PASS";
    public static boolean connected=false;
    static boolean useEncryption=false;
    static KeyPair keyPair;
    static PublicKey pubKey;
    static PrivateKey privKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=26){
            useEncryption=true;
            System.out.println("Trying save mode");
            try {
                keyPair = buildKeyPair();
                pubKey = keyPair.getPublic();
                privKey = keyPair.getPrivate();
            }catch(Exception e){
                e.printStackTrace();
                }
        }else{
            useEncryption=false;
            System.out.println("Insecure mode");
        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.editText2);
        String message2 = editText2.getText().toString();
        EditText passText = (EditText) findViewById(R.id.editText4);
        String password = String.valueOf(passText.getText().toString().hashCode());
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_URL,message2);
        intent.putExtra(EXTRA_PASS,password);
        startActivity(intent);
        connected=true;
    }

    @Override
    protected void onStart(){
        if(connected) {
            EditText editText2 = (EditText) findViewById(R.id.editText2);
            String message2 = editText2.getText().toString();
            Thread nhc = new NetworkHandler();
            //((NetworkHandler) nhc).whocalledme=this;
            ((NetworkHandler) nhc).mestoSend = "#disconnect#";
            ((NetworkHandler) nhc).url = message2;
            nhc.start();
            connected=false;
        }
        super.onStart();
    }

    @Override
    protected void onDestroy(){
        if(connected) {
            EditText editText2 = (EditText) findViewById(R.id.editText2);
            String message2 = editText2.getText().toString();
            Thread nhc = new NetworkHandler();
            //((NetworkHandler) nhc).whocalledme=this;
            ((NetworkHandler) nhc).mestoSend = "#disconnect#";
            ((NetworkHandler) nhc).url = message2;
            nhc.start();
            connected=false;
        }
        super.onDestroy();
    }

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

    @TargetApi(26)
    public static String publicKeyToString(PublicKey p) {

        byte[] publicKeyBytes = p.getEncoded();

        return Base64.getEncoder().encodeToString(publicKeyBytes);

    }

}
