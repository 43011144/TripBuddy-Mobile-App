package com.example.tripbuddy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class gallery extends BaseActivity {

    Button playBtn, pauseBtn, stopBtn, nextBtn;
    TextView nowPlaying;
    MediaPlayer mp;

    @Override
    protected boolean requiresAuth() { return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        playBtn = findViewById(R.id.playBtn);
        stopBtn = findViewById(R.id.stopBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        nextBtn=findViewById(R.id.nextBtn);
        nowPlaying = findViewById(R.id.tvNowPlaying);
        nowPlaying.setText(R.string.tb_stopped);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;
                startActivity(new Intent(gallery.this, memories.class));
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ensurePlayer()) {
                    mp.start();
                    nowPlaying.setText(R.string.tb_playing);
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null && mp.isPlaying()) {
                    mp.pause();
                    nowPlaying.setText(R.string.tb_paused);
                }
            }
        });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp != null) {
                    mp.stop();
                    releasePlayer();
                    nowPlaying.setText(R.string.tb_stopped);
                }
            }
        });
    }

    /** Creates the player once. MediaPlayer.create returns null if the audio resource is missing or fails to load. */
    private boolean ensurePlayer() {
        if (mp != null) return true;
        mp = MediaPlayer.create(this, R.raw.songone);
        if (mp == null) {
            toast(getString(R.string.tb_audio_unavailable));
            return false;
        }
        mp.setOnCompletionListener(player -> {
            releasePlayer();
            nowPlaying.setText(R.string.tb_stopped);
        });
        return true;
    }

    private void releasePlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    // The old version never released the player, which leaked it and kept audio playing after leaving the screen.
    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            nowPlaying.setText(R.string.tb_paused);
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
