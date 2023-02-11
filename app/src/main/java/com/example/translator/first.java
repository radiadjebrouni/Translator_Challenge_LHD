package com.example.translator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.annotation.Nullable;

public class first extends AppCompatActivity {

    private FloatingActionButton b;
    private AppCompatImageView img;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );

        setContentView(R.layout.activity_main);
        b=findViewById ( R.id.f );
        img=findViewById ( R.id.appCompatImageView);

        Animation topAnim = AnimationUtils.loadAnimation(this,R.anim.top_anim);
        img.startAnimation(topAnim);

        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i =new Intent ( first.this,Main.class );
                startActivity ( i );
                overridePendingTransition(R.anim.slidein_right, R.anim.slide_out_left);
            }} );





    }
}
