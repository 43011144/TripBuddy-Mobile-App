package com.example.tripbuddy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class memories extends AppCompatActivity {

    ImageView image1, image2;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memories);


        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        nextBtn=findViewById(R.id.nextBtn);







        View.OnClickListener listener = v -> showFullScreen((ImageView) v);

        image1.setOnClickListener(listener);
        image2.setOnClickListener(listener);
    }

    private void showFullScreen(ImageView imageView) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView fullImage = new ImageView(this);
        fullImage.setImageDrawable(imageView.getDrawable());
        fullImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        dialog.setContentView(fullImage);

        fullImage.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}