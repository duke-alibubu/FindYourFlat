package com.example.androidhdb2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.androidhdb2.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button bto_button, resale_button, sob_button;
    ImageButton help_button , bookmark_button;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userID = getIntent().getStringExtra("UserID");

        bto_button = findViewById(R.id.bto_button);
        resale_button = findViewById(R.id.resale_button);
        sob_button = findViewById(R.id.sob_button);
        help_button = findViewById(R.id.help_button);
        bookmark_button = findViewById(R.id.bookmark_button);

        bto_button.setOnClickListener(this);
        resale_button.setOnClickListener(this);
        sob_button.setOnClickListener(this);
        help_button.setOnClickListener(this);
        bookmark_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.bto_button):
//                Toast.makeText(this, "Open BTO page", Toast.LENGTH_SHORT).show();
                Intent btoIntent = new Intent(this, BtoActivity.class);
                btoIntent.putExtra("UserID",userID);
                startActivity(btoIntent);
                break;
            case (R.id.resale_button):
//                Toast.makeText(this, "Open Resale page", Toast.LENGTH_SHORT).show();
                Intent resaleIntent = new Intent(this, FilterActivity.class);
                resaleIntent.putExtra("UserID", userID);
                resaleIntent.putExtra("SearchType", "Resale");
                startActivity(resaleIntent);
                break;
            case (R.id.sob_button):
//                Toast.makeText(this, "Open SBF page", Toast.LENGTH_SHORT).show();
                Intent sobIntent = new Intent(this, FilterActivitySBF.class);
                sobIntent.putExtra("UserID", userID);
                sobIntent.putExtra("SearchType", "SBF");
                startActivity(sobIntent);
                break;
            case (R.id.help_button):
//                Toast.makeText(this, "Open Help page", Toast.LENGTH_SHORT).show();
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            case (R.id.bookmark_button):
//                Toast.makeText(this, "Open Bookmark page", Toast.LENGTH_SHORT).show();
                Intent bookmarkIntent = new Intent(this, BookMarkActivity.class);
                bookmarkIntent.putExtra("UserID", userID);
                startActivity(bookmarkIntent);
                break;
        }
    }
}