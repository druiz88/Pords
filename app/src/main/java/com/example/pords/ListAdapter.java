package com.example.pords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;


public class ListAdapter extends ArrayAdapter<String>{

    private List<String> users;
    private LayoutInflater mInflater;
    private int mViewResourceId;
    private FloatingActionButton itemfab1, itemfab2, itemfab3;
    private Context context;
    FirebaseDatabase database;
    String playerName, playerID;
    public Deck deck;
    public Map<String, ArrayList<String>> handz;
    public List<String> playerList;


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

    @Override
    public boolean isEnabled(int position){
        return true;
    }


    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int i, View view, @NonNull final ViewGroup viewGroup) {
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

        itemfab1 = view.findViewById(R.id.fab5);
        itemfab2 = view.findViewById(R.id.fab6);
        itemfab3 = view.findViewById(R.id.fab7);
        itemfab3.setEnabled(false);
        itemfab2.setEnabled(false);
        itemfab1.setEnabled(false);

        //Set start button condition
        playerList.put("fab3" + match_id, itemfab3);
        playerList.put("fab2" + match_id, itemfab2);
        playerList.put("fab1" + match_id, itemfab1);


        database.getReference("Ongoing_Matches/" + match_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long count = dataSnapshot.child("Count").getValue(Long.class);
                final Long size = dataSnapshot.child("Size").getValue(Long.class);

                Log.d("count", String.valueOf(count));
                Log.d("size", String.valueOf(size));

                //Check match has started
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                //Check if user is in match
                database.getReference("Users/" + playerID).child("In_Match").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!preferences.contains("match") && dataSnapshot.exists()) {

                            //Check if match is full
                            if(count.equals(size)) {
                                final FloatingActionButton btn3 = playerList.get("fab3" + match_id);
                                assert btn3 != null;
                                btn3.setEnabled(true);
                                DrawableCompat.setTintList(DrawableCompat.wrap(btn3.getDrawable()), ColorStateList.valueOf(Color.WHITE));

                                final FloatingActionButton btn1 = playerList.get("fab1" + match_id);
                                assert btn1 != null;
                                btn1.setEnabled(true);
                                DrawableCompat.setTintList(DrawableCompat.wrap(btn1.getDrawable()), ColorStateList.valueOf(Color.WHITE));
                            }

                            final FloatingActionButton btn2 = playerList.get("fab2" + match_id);
                            assert btn2 != null;
                            btn2.setEnabled(true);
                            DrawableCompat.setTintList(DrawableCompat.wrap(btn2.getDrawable()), ColorStateList.valueOf(Color.WHITE));

                        } else {

                            final FloatingActionButton btn3 = playerList.get("fab3" + match_id);
                            assert btn3 != null;
                            btn3.setEnabled(false);
                            DrawableCompat.setTintList(DrawableCompat.wrap(btn3.getDrawable()), ColorStateList.valueOf(Color.GRAY));

                            final FloatingActionButton btn2 = playerList.get("fab2" + match_id);
                            assert btn2 != null;
                            btn2.setEnabled(false);
                            DrawableCompat.setTintList(DrawableCompat.wrap(btn2.getDrawable()), ColorStateList.valueOf(Color.GRAY));

                            final FloatingActionButton btn1 = playerList.get("fab1" + match_id);
                            assert btn1 != null;
                            btn1.setEnabled(false);
                            DrawableCompat.setTintList(DrawableCompat.wrap(btn1.getDrawable()), ColorStateList.valueOf(Color.GRAY));

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

        //Start Match
        itemfab3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                final DatabaseReference matchPlayersRef = database.getReference("Ongoing_Matches/" + match_id);
                matchPlayersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long size = dataSnapshot.child("Size").getValue(Long.class);
                        deal(size,match_id);
                        matchPlayersRef.child("Round").setValue(0);
                        DatabaseReference matchesRef = database.getReference("Matches_Data/" + match_id);
                        String time = OffsetDateTime.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        DrawableCompat.setTintList(DrawableCompat.wrap(itemfab3.getDrawable()), ColorStateList.valueOf(Color.GRAY));
                        matchesRef.child("Start").setValue(time);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
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
                            Toast.makeText(context, "El usuario ya está en la Partida #" + inmatch, Toast.LENGTH_SHORT).show();
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
        itemfab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference userMatchRef = database.getReference("Users/" + playerID).child("In_Match");
                userMatchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long inmatch = dataSnapshot.getValue(Long.class);
                        if(inmatch!=Long.parseLong(match_id)){
                            Toast.makeText(context, "El usuario está en la Partida #" + inmatch, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, "La partida está llena", Toast.LENGTH_SHORT).show();
                } else {
                    JoinMatch(Long.parseLong(idmatch),bCount);
                    Toast.makeText(context, "Se unió a la partida #" + idmatch, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "Has abandonado la Partida #" + idmatch, Toast.LENGTH_SHORT).show();
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

    private void deal(Long match_size, String match_id){

        playerList = new ArrayList<>();
        deck = new Deck();

        handz = deck.dealHands(match_size.intValue());

        final ArrayList<String> decklist =deck.arrayDeck();

        database.getReference("Ongoing_Matches/" + match_id).child("Players").child("Deck").child("Hand").setValue(decklist.toString());

        final ArrayList<String> Order = new ArrayList<>();
        for (int c = 0; c < match_size; c++) {
            Order.add(String.valueOf(c));
        }
        Collections.shuffle(Order);

        final DatabaseReference playersRef = database.getReference("Ongoing_Matches/" + match_id);

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int z = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerList.clear();
                Iterable<DataSnapshot> players = dataSnapshot.child("Players").getChildren();
                for (DataSnapshot snapshot : players) {
                    String decker = snapshot.getKey();
                    if (!decker.equals("Deck") && !decker.equals("Discard Pile")) {
                        playersRef.child("Players").child(snapshot.getKey()).child("Order").setValue(Long.parseLong(Order.get(z)));
                        playersRef.child("Players").child(snapshot.getKey()).child("Cards").setValue(11);
                        playersRef.child("Players").child(snapshot.getKey()).child("Hand").setValue(handz.get("n" + (Long.parseLong(Order.get(z))+1)).toString());
                        playersRef.child("Players").child(snapshot.getKey()).child("Purchases").setValue(0);
                        playersRef.child("Players").child(snapshot.getKey()).child("Meld").setValue(0);
                        z = z + 1;
                    } else if (decker.equals("Deck")) {
                        Long size = dataSnapshot.child("Size").getValue(Long.class);
                        playersRef.child("Players").child("Deck").child("Cards").setValue(108 - 11*size);
                    }
                }

                final String lcard = decklist.get(decklist.size() - 1);
                decklist.remove(decklist.size() - 1);
                playersRef.child("Players").child("Deck").child("Hand").setValue(decklist.toString());
                playersRef.child("Players").child("Deck").child("Cards").setValue(decklist.size());

                playersRef.child("Players").child("Discard Pile").child("Hand").setValue("[" + lcard + "]");
                playersRef.child("Players").child("Discard Pile").child("Cards").setValue(1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }



}
