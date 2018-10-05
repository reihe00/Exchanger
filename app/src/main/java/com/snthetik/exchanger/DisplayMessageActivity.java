package com.snthetik.exchanger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

public class DisplayMessageActivity extends AppCompatActivity {
    public Thread nh;
    public String currentChat="";
    public String curl;
    private boolean textmodified=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String url = intent.getStringExtra(MainActivity.EXTRA_URL);
        curl=url;
        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message+" on "+url);
        TextView yourTextView = findViewById((R.id.textView2));
        yourTextView.setMovementMethod(new ScrollingMovementMethod());

        try {
            System.out.println("Inside : " + Thread.currentThread().getName());


            nh = new NetworkHandler();
            ((NetworkHandler) nh).whocalledme=this;
            ((NetworkHandler) nh).mestoSend=message;
            ((NetworkHandler)nh).url=url;
            ((NetworkHandler)nh).pass=intent.getStringExtra(MainActivity.EXTRA_PASS);
            nh.start();

        }catch (Exception e){

            System.out.println(e.toString());
        }

        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(200);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                if(textmodified) {
                                    if(currentChat.contains("\nnull")||currentChat.contains("\n#disconnect#")||currentChat==null||currentChat=="\n"){
                                        finish();
                                    }
                                    TextView textView2 = findViewById((R.id.textView2));
                                    textView2.append(currentChat);
                                    currentChat="";

                                    final Layout layout = textView2.getLayout();
                                    if (layout != null) {
                                        int scrollDelta = layout.getLineBottom(textView2.getLineCount() - 1)
                                                - textView2.getScrollY() - textView2.getHeight();
                                        if (scrollDelta > 0)
                                            textView2.scrollBy(0, scrollDelta);
                                    }
                                    textmodified=false;
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();
    }

    public void HeyImFinished(String mitwas){
        System.out.println(mitwas + "wurde empfangen");
        currentChat+="\n" + mitwas;
        currentChat=currentChat.replaceAll("#newline#","\n");
        currentChat=currentChat.replaceAll("#newmessage#","\n");

        textmodified=true;
    }

    public void SendMessage(View view){
        try {
            EditText editText = (EditText) findViewById(R.id.editText3);

            String message = editText.getText().toString();
            System.out.println(message + " soll gesendet werden ö");
            editText.setText("");
            Thread nhc = new NetworkHandler();
            ((NetworkHandler) nhc).whocalledme=this;
            ((NetworkHandler) nhc).mestoSend=message;
            ((NetworkHandler)nhc).url=curl;
            nhc.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    protected void onStop(){

        Thread nhc = new NetworkHandler();
        ((NetworkHandler) nhc).whocalledme=this;
        ((NetworkHandler) nhc).mestoSend="#disconnect#";
        ((NetworkHandler)nhc).url=curl;
        nhc.start();
        super.onStop();
    }*/


}
