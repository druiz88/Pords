package com.example.pords;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LobbyActivity extends AppCompatActivity {

    private Spinner spinner;
    ListView listView;
    List<String> matchList;
    TextView tvName;
    Button lob_create;
    Long lastMatch, MatchID;
    String nPlayers, Time, playerName, playerID;
    FirebaseDatabase database;
    com.example.pords.ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Declare variables
        database = FirebaseDatabase.getInstance();

        Intent i = getIntent();
        playerName = i.getStringExtra("player");
        playerID = i.getStringExtra("key");

        listView = findViewById(R.id.listView);
        lob_create = findViewById(R.id.lob_create);
        tvName = findViewById(R.id.tvName);

        tvName.setText(playerName);

        matchList = new ArrayList<>();

        //Create Match
        lob_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Detect last match
                DatabaseReference matchRef = database.getReference("Matches");
                matchRef.orderByChild("Matches").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            lastMatch = Long.parseLong(Objects.requireNonNull(postSnapshot.getKey()));
                        }
                        if(lastMatch==null){
                            MatchID = 5471650401L;
                        } else {
                            MatchID = lastMatch + 1;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                //Player number builder
                final AlertDialog.Builder diag = new AlertDialog.Builder(LobbyActivity.this);
                @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                spinner = mView.findViewById(R.id.spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LobbyActivity.this,
                        android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.numlist));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                diag.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(spinner.getSelectedItem().toString().equalsIgnoreCase("Pick players number")){
                            Toast.makeText(LobbyActivity.this, "Please pick a player number", Toast.LENGTH_SHORT).show();
                        } else {
                            RegisterLobby(MatchID);
                            Toast.makeText(LobbyActivity.this, "Match created", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                diag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                diag.setView(mView);
                AlertDialog dialg = diag.create();
                dialg.show();
            }
        });

        addRoomsEventListener();
    }


    private void addRoomsEventListener(){
        DatabaseReference matchesRef = database.getReference("Matches");
        matchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                matchList.clear();
                Iterable<DataSnapshot> matches = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : matches){
                    matchList.add(snapshot.getKey());
                    adapter = new com.example.pords.ListAdapter(LobbyActivity.this, R.layout.activity_list_adapter, matchList, playerName);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void RegisterLobby(Long matchID) {

        nPlayers = spinner.getSelectedItem().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Time = sdf.format(new Date());

        DatabaseReference matchRef = database.getReference("Matches/" + matchID);
        matchRef.child("Count").setValue(1);
        matchRef.child("Size").setValue(Long.parseLong(nPlayers));
        matchRef.child("Players").child(playerName).child("Hand").setValue("-");

        DatabaseReference matchData = database.getReference("Match Data/" + matchID);
        matchData.child("Player1").setValue(playerName);
        matchData.child("Starts").setValue(Time);
    }

}
