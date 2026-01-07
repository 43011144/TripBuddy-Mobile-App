package com.example.tripbuddy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class gallery extends AppCompatActivity {

    Button playBtn, pauseBtn, stopBtn, nextBtn;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        playBtn = findViewById(R.id.playBtn);
        stopBtn = findViewById(R.id.stopBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        nextBtn=findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp == null) {
                    mp = MediaPlayer.create(gallery.this, R.raw.songone);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.release();
                            mp = null;
                        }
                    });
                }
                mp.start();
                Intent intent=new Intent(gallery.this, memories.class);


            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp == null) {
                    mp = MediaPlayer.create(gallery.this, R.raw.songone);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.release();
                            mp = null;
                        }
                    });
                }
                mp.start();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null && mp.isPlaying()) {
                    mp.pause();
                }
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.stop(); //
                    mp.release();
                    mp = null;
                }
            }
        });
    }
}