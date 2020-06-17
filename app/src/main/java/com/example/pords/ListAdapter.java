package com.example.pords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;


public class ListAdapter extends ArrayAdapter<String>{

    private List<String> users;
    private LayoutInflater mInflater;
    private int mViewResourceId;
    private FloatingActionButton itemfab, itemfab2, itemfab3;
    private Context context;
    FirebaseDatabase database;
    String playerName, playerID;


    public ListAdapter(Context context, int textViewResourceId, List<String> userList, String playa, String playaID) {
        super(context, textViewResourceId);
        this.context = context;
        this.users = userList;
        this.playerName = playa;
        this.playerID = playaID;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    @Override
    public int getCount() {
        return users.size();
    }


    @Override
    public long getItemId(int i) {
        return i;
    }


    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        view = mInflater.inflate(mViewResourceId, null);

        final Map<String, FloatingActionButton> playerList = new HashMap<>();

        final String match_id = users.get(i);
        database = FirebaseDatabase.getInstance();

        if (users != null) {
            TextView name = view.findViewById(R.id.textName);
            if (name != null) {
                name.setText(match_id);
            }
        }

        itemfab = view.findViewById(R.id.fab5);
        itemfab2 = view.findViewById(R.id.fab6);
        itemfab3 = view.findViewById(R.id.fab7);
        itemfab3.setEnabled(false);

        //Set start button condition
        playerList.put("fab" + users.get(i), itemfab3);

            //Check if match is full
        database.getReference("Ongoing_Matches/" + match_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long count = dataSnapshot.child("Count").getValue(Long.class);
                Long size = dataSnapshot.child("Size").getValue(Long.class);
                assert count != null;
                if(count.equals(size)){
                    //Check if user is in match
                    database.getReference("Users/" + playerID).child("In_Match").addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final Long matched = dataSnapshot.getValue(Long.class);
                            if(matched!=null && matched.equals(Long.parseLong(match_id))){
                                final FloatingActionButton btn = playerList.get("fab" + matched);
                                assert btn != null;
                                btn.setEnabled(true);
                                DrawableCompat.setTintList(DrawableCompat.wrap(btn.getDrawable()), ColorStateList.valueOf(Color.BLUE));
                            } else {
                                final FloatingActionButton btn = playerList.get("fab" + match_id);
                                assert btn != null;
                                btn.setEnabled(false);
                                DrawableCompat.setTintList(DrawableCompat.wrap(btn.getDrawable()), ColorStateList.valueOf(Color.GRAY));
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

        //Start Match
        itemfab3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                final DatabaseReference startRef = database.getReference("Match_Data/" + match_id);
                String time = OffsetDateTime.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                startRef.child("Start").setValue(time);
                Intent intent = new Intent(context, MatchActivity.class);
                intent.putExtra("match", match_id);
                intent.putExtra("player", playerName);
                intent.putExtra("playerID", playerID);
                context.startActivity(intent);
            }
        });


        //Join match
        itemfab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if user is in a match
                final DatabaseReference userMatchRef = database.getReference("Users/" + playerID).child("In_Match");
                userMatchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long inmatch = dataSnapshot.getValue(Long.class);
                        if(inmatch!=null){
                            Toast.makeText(context, "User already in Match #" + inmatch, Toast.LENGTH_SHORT).show();
                        } else {
                            //Update match status
                            updateJoinedMatchStats(match_id);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        //Abandon match
        itemfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference userMatchRef = database.getReference("Users/" + playerID).child("In_Match");
                userMatchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long inmatch = dataSnapshot.getValue(Long.class);
                        if(inmatch!=Long.parseLong(match_id)){
                            Toast.makeText(context, "User is in Match #" + inmatch, Toast.LENGTH_SHORT).show();
                        } else {
                            //Update match status
                            updateAbandonedMatchStats(match_id);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });



        return view;
    }

    private void updateJoinedMatchStats(final String idmatch){

        final DatabaseReference countRef = database.getReference("Ongoing_Matches/" + idmatch);
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long aCount = snapshot.child("Count").getValue(Long.class);
                Long aSize = snapshot.child("Size").getValue(Long.class);
                Long bCount = aCount +1;
                if(aCount.equals(aSize)){
                    Toast.makeText(context, "This Match is full", Toast.LENGTH_SHORT).show();
                } else {
                    JoinMatch(Long.parseLong(idmatch),bCount);
                    Toast.makeText(context, "Joined Match #" + idmatch, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void JoinMatch(Long matchID, Long pCount) {

        DatabaseReference matchRef = database.getReference("Ongoing_Matches/" + matchID);
        matchRef.child("Count").setValue(pCount);
        matchRef.child("Players").child(playerName).child("Hand").setValue("-");

        DatabaseReference matchData = database.getReference("Matches_Data/" + matchID);
        matchData.child("Player"+pCount).setValue(playerName);

        DatabaseReference userData = database.getReference("Users/" + playerID);
        userData.child("In_Match").setValue(matchID);
    }

    private void updateAbandonedMatchStats(final String idmatch){

        final DatabaseReference countRef = database.getReference("Ongoing_Matches/" + idmatch);
        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Long aCount = snapshot.child("Count").getValue(Long.class);
                Long bCount = aCount -1;
                countRef.child("Count").setValue(bCount);
                countRef.child("Players").child(playerName).removeValue();
                DatabaseReference userData = database.getReference("Users/" + playerID);
                userData.child("In_Match").removeValue();

                final DatabaseReference matchData = database.getReference("Matches_Data/" + idmatch);
                matchData.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> players = new ArrayList();
                        for(DataSnapshot snaps: dataSnapshot.getChildren()){
                            if(!snaps.getKey().equals("Created") && !snaps.getKey().equals("Size")){
                                if(!snaps.getValue(String.class).equals(playerName)) {
                                    players.add(snaps.getValue(String.class));
                                    snaps.getRef().removeValue();
                                } else {
                                    snaps.getRef().removeValue();
                                }
                            }
                        }
                        for(int i=0; i<players.size(); i++){
                            matchData.child("Player" + (i+1)).setValue(players.get(i));
                        }
                        Toast.makeText(context, "User abandoned Match #" + idmatch, Toast.LENGTH_SHORT).show();
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
    }

}
