package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class registration extends BaseActivity {


    Button button3, button4,button5, button6, button7, button8;

    @Override
    protected boolean requiresAuth() { return false; }  // the menu itself is open; each feature checks sign-in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.activity_registration);

        button3= findViewById(R.id.button3);
        button4= findViewById(R.id.button4);
        button5= findViewById(R.id.button5);
        button6= findViewById(R.id.button6);
        button7= findViewById(R.id.button7);
        button8= findViewById(R.id.button8);

        //My Trips (trip history stored in the database)
        button7.setOnClickListener(v -> {
            if (!isDoubleClick()) open(TripHistoryActivity.class);
        });

        //Settings (theme, account)
        button8.setOnClickListener(v -> {
            if (!isDoubleClick()) open(SettingsActivity.class);
        });

        //Plan trip(Budget Class)
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;
                Intent intent=new Intent(registration.this, budget.class );
                startActivity(intent);


            }
        });

        //Create Memory
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;

             Intent intent=new Intent(registration.this, memories.class );
                startActivity(intent);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;
                Intent intent=new Intent(registration.this, LoginActivity.class );
                startActivity(intent);


            }
        });

        //View Gallery
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;

                Intent intent=new Intent(registration.this, gallery.class );
                startActivity(intent);

            }
        });
    }
}