package com.example.translator;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import javax.annotation.Nullable;

public class SplashScreenActivty extends AppCompatActivity {
    private static int splashTimeOut=3000;
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
              ImageView logo;
            TextView every;
            setContentView(R.layout.activity_translate_main);
            logo=(ImageView)findViewById(R.id.img);

            new Handler ().postDelayed( new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashScreenActivty.this,first.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slidein_right, R.anim.slide_out_left);
                    finish();
                }
            },splashTimeOut);

            Animation topAnim = AnimationUtils.loadAnimation(this,R.anim.top_anim);
            logo.startAnimation(topAnim);



    }
}