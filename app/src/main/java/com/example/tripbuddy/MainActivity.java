package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends BaseActivity {

    ImageView LogoImage;
    TextView subtitle;

    @Override
    protected boolean requiresAuth() { return false; }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LogoImage= findViewById(R.id.LogoImage);
        subtitle = findViewById(R.id.tvSubtitle);
        subtitle.setText(session.isValid()
                ? getString(R.string.tb_welcome_back, session.username())
                : getString(R.string.tb_tagline));

         LogoImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (isDoubleClick()) return;

                 Intent intent= new Intent(MainActivity.this, home.class);
                 startActivity(intent);
             }
         });

    }
}