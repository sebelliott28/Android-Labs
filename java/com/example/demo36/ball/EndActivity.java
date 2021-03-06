package com.example.demo36.ball;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EndActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endpage);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        // This code enables the FULLSCREEN MODE
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        final Button startnewgame = findViewById(R.id.buttonnewgame);
        startnewgame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code here to excecutes main thread after user press the button
                startActivity(new Intent(EndActivity.this, MainActivity.class));
            }
        });


        final Button backtohomebutton = findViewById(R.id.buttontohome);
        backtohomebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code here to excecutes main thread after user press the button
                startActivity(new Intent(EndActivity.this, StartActivity.class));
            }
        });


    }
}
