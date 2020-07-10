package com.example.pords;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LobbyActivity extends AppCompatActivity {

    private Spinner spinner;
    ListView listView;
    List<String> matchList;
    TextView tvName;
    Button lob_create, lob_abandon;
    Long lastMatch, detected_match, inmatch;
    String nPlayers, Time, playerName, playerID;
    FirebaseDatabase database;
    com.example.pords.ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Declare variables
        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        playerName = preferences.getString("player", null);
        playerID = preferences.getString("key", null);

        listView = findViewById(R.id.listView);
        lob_create = findViewById(R.id.lob_create);
        lob_abandon = findViewById(R.id.lob_abandon);
        tvName = findViewById(R.id.tvName);

        String splat = "Usuario: " + playerName;
        tvName.setText(splat);

        matchList = new ArrayList<>();


        //Send users in match to activity
        final DatabaseReference userRef = database.getReference("Users/" + playerID).child("In_Match");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long match_id = dataSnapshot.getValue(Long.class);
                final DatabaseReference ongmatchRef = database.getReference("Matches_Data/" + match_id).child("Start");
                ongmatchRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Intent intent = new Intent(LobbyActivity.this, MatchActivity.class);
                            intent.putExtra("playerName", playerName);
                            intent.putExtra("playerID", playerID);
                            intent.putExtra("match_id", String.valueOf(match_id));
                            startActivity(intent);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        //Create Match
        lob_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if user is in a match
                final DatabaseReference userMatchRef = database.getReference("Users/" + playerID).child("In_Match");
                userMatchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        inmatch = dataSnapshot.getValue(Long.class);
                        if(inmatch!=null){
                            Toast.makeText(LobbyActivity.this, "El usuario ya está en la Partida #" + inmatch, Toast.LENGTH_SHORT).show();
                        } else {
                            //Check last match
                            DatabaseReference matchRef = database.getReference("Matches_Data");

                            matchRef.orderByChild("Matches_Data").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        lastMatch = Long.parseLong(Objects.requireNonNull(postSnapshot.getKey()));
                                    }
                                    if(lastMatch==null){
                                        detected_match = 5471650401L;
                                    } else {
                                        detected_match = lastMatch + 1;
                                    }
                                    dialogBuilder(detected_match);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

        });


        lob_abandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference userLogRef = database.getReference("Users/" + playerID).child("Logged");

                userLogRef.setValue("Off");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();

                if(preferences.contains("checked") && preferences.getBoolean("checked", false)) {
                    editor.putBoolean("checked", false);
                    editor.apply();
                }

                Intent intent = new Intent(LobbyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        addRoomsEventListener();
    }


    private void addRoomsEventListener(){

        DatabaseReference matchesRef = database.getReference("Ongoing_Matches");
        matchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                matchList.clear();
                Iterable<DataSnapshot> matches = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : matches){
                    matchList.add(snapshot.getKey());
                }

                adapter = new com.example.pords.ListAdapter(LobbyActivity.this, R.layout.activity_list_adapter, matchList, playerName, playerID);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        //Check if user is in a match
                        final DatabaseReference inMatchRef = database.getReference("Users/" + playerID).child("In_Match");

                        inMatchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){

                                    final Long in_match = dataSnapshot.getValue(Long.class);

                                    //Check if match has started
                                    final DatabaseReference ongmatchRef = database.getReference("Matches_Data/" + in_match).child("Start");
                                    ongmatchRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.exists()) {

                                                final int index = matchList.indexOf(String.valueOf(in_match));

                                                for(int x=0; x<listView.getCount(); x++) {
                                                    if(x!=index) {
                                                        listView.getChildAt(x).setEnabled(false);
                                                        listView.getChildAt(x).setClickable(false);
                                                    }
                                                }

                                                Log.d("match", String.valueOf(in_match));
                                                Intent intent = new Intent(LobbyActivity.this, MatchActivity.class);
                                                intent.putExtra("playerName", playerName);
                                                intent.putExtra("playerID", playerID);
                                                intent.putExtra("match_id", String.valueOf(in_match));
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(LobbyActivity.this, "El juego todavía no ha comenzado", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void RegisterLobby(Long matchID) {

        nPlayers = spinner.getSelectedItem().toString();
        Time = OffsetDateTime.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        DatabaseReference matchRef = database.getReference("Ongoing_Matches/" + matchID);
        matchRef.child("Count").setValue(1);
        matchRef.child("Size").setValue(Long.parseLong(nPlayers));
        matchRef.child("Players").child(playerName).child("Hand").setValue("-");

        DatabaseReference matchData = database.getReference("Matches_Data/" + matchID);
        matchData.child("Player1").setValue(playerName);
        matchData.child("Created").setValue(Time);
        matchData.child("Size").setValue(Long.parseLong(nPlayers));

        DatabaseReference userData = database.getReference("Users/" + playerID);
        userData.child("In_Match").setValue(matchID);

    }


    public void dialogBuilder(final Long match_id){
        final AlertDialog.Builder diag = new AlertDialog.Builder(LobbyActivity.this);
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        spinner = mView.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LobbyActivity.this,
                android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.numlist));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        diag.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(spinner.getSelectedItem().toString().equalsIgnoreCase("Pick players number")){
                    Toast.makeText(LobbyActivity.this, "Por favor elija un número de jugadores", Toast.LENGTH_SHORT).show();
                } else {
                    RegisterLobby(match_id);
                    Toast.makeText(LobbyActivity.this, "Partida #" + match_id + " creada", Toast.LENGTH_SHORT).show();
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

}
