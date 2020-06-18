package com.example.pords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MatchActivity extends AppCompatActivity {

    String match_id, playerName, playerID, role;
    Deck deck;
    FirebaseDatabase database;
    List<String> playerList;
    TextView textView;
    ArrayList<String> playerHand;
    Button btn_start;
    LinearLayout linear;
    Map<String, ImageView> handsMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        //Declare variables
        deck = new Deck();
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();
        btn_start = findViewById(R.id.btn_start);
        linear = findViewById(R.id.linear);

        //Get intents
        Intent intent = getIntent();
        match_id = intent.getStringExtra("match_id");
        playerName = intent.getStringExtra("playerName");
        playerID = intent.getStringExtra("playerID");
        role = intent.getStringExtra("role");

        //Set username textview
        textView = findViewById(R.id.textView);
        String username = "User: " + playerName;
        textView.setText(username);


        //Get cards values
        DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id).child("Round");
        roundRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id);
                roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                        Long cards = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                        Long round = dataSnapshot.child("Round").getValue(Long.class);
                        Long nplayers = dataSnapshot.child("Size").getValue(Long.class);
                        Long order = dataSnapshot.child("Players").child(playerName).child("Order").getValue(Long.class);

                        if(order.equals(round%nplayers)){
                            btn_start.setEnabled(true);
                        } else {
                            btn_start.setEnabled(false);
                        }

                        if(hand!=null && !hand.equals("-")) {
                            Log.d("round", String.valueOf(round));
                            Log.d("hand", hand);
                            Log.d("cards", String.valueOf(cards));
                            String num = hand.substring(1, hand.length() - 1);
                            String[] str = num.split(", ");
                            playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));
                            drawCards(playerHand, cards);
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

    }

    public void drawCards(ArrayList<String> playaHand, Long ncards){
        //Draw cards
        ViewGroup view = (ViewGroup)findViewById(R.id.linear);

        view.removeAllViews();

        handsMap = new HashMap<>();

        for(int n=0; n<ncards.intValue(); n++){
            ImageView iv = new ImageView(this);
            handsMap.put("img" + (n+1), iv);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, 200);
            if(n==0) {
                layoutParams.setMarginStart(60);
            } else {
                layoutParams.setMarginStart(-60);
            }
            layoutParams.gravity = Gravity.BOTTOM;
            handsMap.get("img" + (n+1)).setLayoutParams(layoutParams);
            handsMap.get("img" + (n+1)).setImageResource(R.drawable.gray_back);
            handsMap.get("img" + (n+1)).setAdjustViewBounds(true);

            String PACKAGE_NAME = getApplicationContext().getPackageName();
            int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+playaHand.get(n) , null, null);
            handsMap.get("img" + (n+1)).setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));

            final int finalN = n;
            handsMap.get("img" + (n+1)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) handsMap.get("img" + (finalN +1)).getLayoutParams();
                    if(params.gravity== Gravity.TOP){
                        params.gravity = Gravity.BOTTOM;
                    } else {
                        params.gravity = Gravity.TOP;
                    }
                    handsMap.get("img" + (finalN +1)).setLayoutParams(params);
                }
            });

            view.addView(handsMap.get("img" + (n+1)));
        }
    }

    public void Sort(View views){
        Collections.sort(playerHand);
        updateHand(playerHand.toString());

        DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id);
        roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                Long cards = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                Long round = dataSnapshot.child("Round").getValue(Long.class);
                Long nplayers = dataSnapshot.child("Size").getValue(Long.class);
                Long order = dataSnapshot.child("Players").child(playerName).child("Order").getValue(Long.class);

                if(order.equals(round%nplayers)){
                    btn_start.setEnabled(true);
                } else {
                    btn_start.setEnabled(false);
                }

                if(hand!=null && !hand.equals("-")) {
                    Log.d("round", String.valueOf(round));
                    Log.d("hand", hand);
                    Log.d("cards", String.valueOf(cards));
                    String num = hand.substring(1, hand.length() - 1);
                    String[] str = num.split(", ");
                    playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));
                    drawCards(playerHand, cards);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void updateHand(String vHand){
        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id).child("Players").child(playerName).child("Hand");
        handRef.setValue(vHand);
    }

    public void drawCard(){
        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id);

        handRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String hand = dataSnapshot.child("Players").child("Deck").child("Hand").getValue(String.class);
                Long dcards = dataSnapshot.child("Players").child("Deck").child("Cards").getValue(Long.class);
                assert hand != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");
                assert dcards != null;
                Log.d("dcards", dcards.toString());
                ArrayList<String> decklist = new ArrayList<>(Arrays.asList(str).subList(0, dcards.intValue()));
                String lcard = decklist.get(dcards.intValue()-1);
                decklist.remove(dcards.intValue()-1);
                Log.d("deck hand", decklist.toString());
                Log.d("deck cards", String.valueOf(dcards.intValue()-1));


                String hand2 = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                Long dcards2 = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                assert hand2 != null;
                String num2 = hand2.substring(1,hand2.length()-1);
                String[] str2 = num2.split(", ");
                assert dcards2 != null;
                playerHand = new ArrayList<>(Arrays.asList(str2).subList(0, dcards2.intValue()));
                playerHand.add(lcard);
                Log.d("Player cards", String.valueOf(playerHand.size()));
                Log.d("Hand", playerHand.toString());

                handRef.child("Players").child("Deck").child("Hand").setValue(decklist.toString());
                handRef.child("Players").child("Deck").child("Cards").setValue(dcards.intValue()-1);
                handRef.child("Players").child(playerName).child("Cards").setValue(playerHand.size());
                handRef.child("Players").child(playerName).child("Hand").setValue(playerHand.toString());

                final DatabaseReference matchesRef = database.getReference("Ongoing_Matches/" + match_id).child("Round");
                matchesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long round = dataSnapshot.getValue(Long.class);
                        Log.d("setround", round.toString());
                        matchesRef.setValue(round+1);
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

    public void Start(View view){
        drawCard();
    }


    public void Abandon2(View view){

    }

}
