package com.example.pords;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    private Spinner spinner;
    private ListView listView;
    private List<String> matchList;
    private TextView tvName;
    Button lob_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Declare Variables
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Intent i = getIntent();
        String playerName = i.getStringExtra("player");
        String playerID = i.getStringExtra("key");

        listView = findViewById(R.id.listView);
        lob_create = findViewById(R.id.lob_create);
        tvName = findViewById(R.id.tvName);

        tvName.setText(playerName);

        matchList = new ArrayList<>();




    }
}
