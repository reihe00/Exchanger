package com.snthetik.exchanger;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.snthetik.exchanger.MESSAGE";
    public static final String EXTRA_URL = "com.snthetik.exchanger.URL";
    public static final String EXTRA_PASS = "com.snthetik.exchanger.PASS";

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_URL = "Url";

    private final String DefaultUnameValue = "";
    private String UnameValue;

    private final String DefaultUrlValue = "snthetik.com";
    private String UrlValue;

    public static boolean connected=false;
    static boolean useEncryption=false;
    static KeyPair keyPair;
    static PublicKey pubKey;
    static PrivateKey privKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadPreferences();
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
        savePreferences();
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
        }else{
            NetworkHandler.aesKey=null;
            NetworkHandler.serverKey=null;
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

    public static byte[] encryptAES(SecretKey aeskey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, aeskey);
        byte[] byteCipherText = aesCipher.doFinal(message.getBytes("UTF8"));
        return byteCipherText;
    }

    @TargetApi(26)
    public static String decryptAES(SecretKey secKey, String answer) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        byte[] code=Base64.getDecoder().decode(answer);
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secKey);
        byte[] bytePlainText = aesCipher.doFinal(code);
        String plainText = new String(bytePlainText);
        return plainText;
    }

    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.editText2);
        String message2 = editText2.getText().toString();
        // Edit and commit
        UnameValue = message;
        UrlValue = message2;

        editor.putString(PREF_UNAME, UnameValue);
        editor.putString(PREF_URL, UrlValue);
        editor.commit();
    }

    private void loadPreferences() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        // Get value
        UnameValue = settings.getString(PREF_UNAME, DefaultUnameValue);
        UrlValue = settings.getString(PREF_URL, DefaultUrlValue);
        EditText edt_username = (EditText) findViewById(R.id.editText);
        EditText edt_url = (EditText) findViewById(R.id.editText2);
        edt_username.setText(UnameValue);
        edt_url.setText(UrlValue);
        System.out.println("onResume load name: " + UnameValue);
        System.out.println("onResume load password: " + UrlValue);
    }

}
