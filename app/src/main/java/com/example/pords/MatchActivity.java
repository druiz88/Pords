package com.example.pords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MatchActivity extends AppCompatActivity {

    String match_id, playerName, playerID, role;
    Deck deck;
    FirebaseDatabase database;
    List<String> playerList;
    ImageView[] imageViews = new ImageView[11];
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        deck = new Deck();
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();

        Intent intent = getIntent();
        match_id = intent.getStringExtra("match");
        playerName = intent.getStringExtra("playerName");
        playerID = intent.getStringExtra("playerID");
        role = intent.getStringExtra("role");

        textView = findViewById(R.id.textView);
        String username = "User: " + playerName;
        textView.setText(username);

        for(int u = 0; u < 11; u++){
            String imageID = "imageView" + (u+1);
            int resID = getResources().getIdentifier(imageID,"id",getPackageName());
            imageViews[u] = findViewById(resID);
        }


    }

    public void Sort(View view){
    }

    public void Abandon2(View view){

    }

}
