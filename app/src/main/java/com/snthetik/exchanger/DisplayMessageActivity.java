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
import java.util.ArrayList;


public class DisplayMessageActivity extends AppCompatActivity {
    public Thread nh;
    public String currentChat="";
    public static String curl;
    private boolean textmodified=false;
    public static String currentScope="";
    public static ArrayList<String> allUsers = new ArrayList<String>();
    public static ArrayList<String> allChats = new ArrayList<String>();
    private int currentChatId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkHandler.aesKey=null;
        NetworkHandler.serverKey=null;
        NetworkHandler.alreadylistening=false;
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String url = intent.getStringExtra(MainActivity.EXTRA_URL);
        curl=url;
        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(url);
        currentChat+=url+"\n";
        currentScope=url;
        allChats.add(url+"\n");
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
                                    if(currentChat.endsWith("\nnull")||currentChat.contains("\n#disconnect#")||currentChat==null||currentChat=="\n"){

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
                                TextView textView = findViewById(R.id.textView);
                                if(!textView.getText().toString().equalsIgnoreCase(currentScope)) {
                                    System.out.println("Scope has changed to " + currentScope);
                                    textView.setText(currentScope);
                                    TextView textView2 = findViewById((R.id.textView2));
                                    if(textView2.getText().toString().startsWith(allChats.get(currentChatId)))
                                    allChats.set(currentChatId,textView2.getText().toString());
                                    else{
                                        for(int i=0;i<allChats.size();i++){
                                            if(textView2.getText().toString().startsWith(allChats.get(i))){
                                                allChats.set(i,textView2.getText().toString());
                                                break;
                                            }
                                        }
                                    }
                                    boolean needstill = true;
                                    for(int i=0;i<allChats.size();i++){
                                        if(allChats.get(i).startsWith(currentScope)){
                                            currentChatId=i;
                                            textView2.setText(allChats.get(i));
                                            needstill=false;
                                            break;
                                        }
                                    }
                                    if(needstill){
                                        allChats.add(currentScope+"\n");
                                        currentChatId=allChats.size()-1;
                                        textView2.setText(allChats.get(allChats.size()-1));
                                    }
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

    public void HeyImFinished(String antwort){
        String[] allmsgs = antwort.split("#newmessage#");

        for(String mitwas : allmsgs) {
            if (mitwas.startsWith("#user#")) {
                allUsers.clear();
                String[] curregus = mitwas.split("#user#");
                for (String s : curregus) {
                    if (s.length() > 1 && !s.equalsIgnoreCase(NetworkHandler.username)) {
                        allUsers.add(s);
                        System.out.println(s + " added");
                    }
                }
            } else if (mitwas.startsWith("#from#")) {
                System.out.println(mitwas + "wurde empfangen");
                mitwas = mitwas.replaceAll("#from#", "");
                int tID = getThisID(mitwas);
                if (currentChatId == tID) {

                    currentChat += "\n" + mitwas;
                    currentChat = currentChat.replaceAll("#newline#", "\n");
                    currentChat = currentChat.replaceAll("#newmessage#", "\n");

                    textmodified = true;
                } else {
                    String modify = "\n" + mitwas;
                    modify = modify.replaceAll("#newline#", "\n");
                    modify = modify.replaceAll("#newmessage#", "\n");
                    allChats.set(tID, allChats.get(tID) + modify);
                }
            } else {
                System.out.println(mitwas + "wurde empfangen");
                if (currentChatId == 0) {
                    currentChat += "\n" + mitwas;
                    currentChat = currentChat.replaceAll("#newline#", "\n");
                    currentChat = currentChat.replaceAll("#newmessage#", "\n");

                    textmodified = true;
                } else {
                    String modify = "\n" + mitwas;
                    modify = modify.replaceAll("#newline#", "\n");
                    modify = modify.replaceAll("#newmessage#", "\n");
                    allChats.set(0, allChats.get(0) + modify);
                }
            }
        }
    }

    public void SendMessage(View view){                     //TODO enabling sending pictures
        try {
            EditText editText = (EditText) findViewById(R.id.editText3);

            String message = editText.getText().toString();
            System.out.println(message + " soll gesendet werden");
            editText.setText("");
            Thread nhc = new NetworkHandler();
            ((NetworkHandler) nhc).whocalledme=this;
            ((NetworkHandler) nhc).mestoSend=message;
            ((NetworkHandler)nhc).url=curl;
            nhc.start();
            if(!currentScope.equalsIgnoreCase(curl)){
                currentChat += "\n" + message;
                currentChat = currentChat.replaceAll("#newline#", "\n");
                currentChat = currentChat.replaceAll("#newmessage#", "\n");

                textmodified = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SelectUser(View view){
        Intent intent = new Intent(this, PickUser.class);

        startActivity(intent);
    }

    private int getThisID(String mes){
        String[] messplit = mes.split(":");
        for(int i=0;i<allChats.size();i++){
            if(allChats.get(i).startsWith(messplit[0])){
                return i;

            }
        }
        allChats.add(messplit[0]+"\n");
        return allChats.size()-1;
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
