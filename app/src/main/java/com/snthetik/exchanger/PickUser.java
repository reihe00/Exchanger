package com.snthetik.exchanger;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class PickUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_user);





        LinearLayout l_layout = (LinearLayout) findViewById(R.id.linear_layout);
        l_layout.setOrientation(LinearLayout.VERTICAL);

        for(String s : DisplayMessageActivity.allUsers) {
            Button btn1 = new Button(this);
            btn1.setText(s);

            l_layout.addView(btn1);

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // put code on click operation
                    Button b = (Button)v;
                    String buttonText = b.getText().toString();
                    DisplayMessageActivity.currentScope=buttonText;
                    finish();
                }
            });
        }
    }

    public void AllButtonClicked(View v){
            DisplayMessageActivity.currentScope=DisplayMessageActivity.curl;
            finish();
    }
}
