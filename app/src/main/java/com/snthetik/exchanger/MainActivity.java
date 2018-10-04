package com.snthetik.exchanger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.snthetik.exchanger.MESSAGE";
    public static final String EXTRA_URL = "com.snthetik.exchanger.URL";
    public static final String EXTRA_PASS = "com.snthetik.exchanger.PASS";
    public static boolean connected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

}
