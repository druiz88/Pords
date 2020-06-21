package com.example.pords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
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
    Button btn_start, btn_disc, btn_buy;
    LinearLayout linear;
    Map<String, ImageView> handsMap;
    private static final long START_TIME_IN_MILLIS = 15000;
    private TextView mTextViewCountDown;
    CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    ImageView discardPile;
    boolean[] array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        //Declare variables
        deck = new Deck();
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();
        btn_start = findViewById(R.id.btn_start);
        btn_disc = findViewById(R.id.btn_disc);
        btn_buy = findViewById(R.id.btn_buy);
        linear = findViewById(R.id.linear);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mTextViewCountDown.setVisibility(View.GONE);
        discardPile = findViewById(R.id.imageViewDiscard);

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

        discardPile(match_id);

        //Get cards values
        //Listen values on Round change
        DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id).child("Round");
        roundRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id);
                roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long cards = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                        Long round = dataSnapshot.child("Round").getValue(Long.class);
                        Long nplayers = dataSnapshot.child("Size").getValue(Long.class);
                        Long order = dataSnapshot.child("Players").child(playerName).child("Order").getValue(Long.class);
                        assert order != null;

                        //Enable DRAW/DISCARD buttons only on player's turn
                        if(round!=null && nplayers!=null && order.equals(round%nplayers)){
                            btn_start.setEnabled(true);
                            btn_disc.setEnabled(true);
                        } else {
                            btn_start.setEnabled(false);
                            btn_disc.setEnabled(false);
                        }

                        String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);

                        //Generate hand only on this conditions
                        if(hand!=null && !hand.equals("-")) {
                            Log.d("round", String.valueOf(round));
                            Log.d("hand", hand);
                            Log.d("cards", String.valueOf(cards));
                            String num = hand.substring(1, hand.length() - 1);
                            String[] str = num.split(", ");
                            assert cards != null;
                            playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));
                            drawCards(playerHand, cards);

                            String disHand = dataSnapshot.child("Players").child("Discard Pile").child("Hand").getValue(String.class);
                            String realLast = disHand.substring(disHand.length() - 5, disHand.length() - 1);
                            Log.d("realLast", realLast);
                            String PACKAGE_NAME = getApplicationContext().getPackageName();
                            int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+realLast , null, null);
                            discardPile.setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));
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

    public void discardPile(String matchid){
        //Get Discard Pile
        final DatabaseReference deckRef = database.getReference("Ongoing_Matches/" + matchid).child("Players");
        deckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long deckCards = dataSnapshot.child("Deck").child("Cards").getValue(Long.class);
                final String deckHand = dataSnapshot.child("Deck").child("Hand").getValue(String.class);

                Log.d("deckHand", deckHand);

                String num = deckHand.substring(1, deckHand.length() - 1);
                String[] str = num.split(", ");
                ArrayList<String> decklist = new ArrayList<>(Arrays.asList(str).subList(0, deckCards.intValue()));
                final String lcard = decklist.get(deckCards.intValue()-1);
                decklist.remove(deckCards.intValue()-1);
                deckRef.child("Deck").child("Hand").setValue(decklist.toString());
                deckRef.child("Deck").child("Cards").setValue(decklist.size());
                Log.d("deckHand2", decklist.toString());

                deckRef.child("Discard Pile").child("Hand").setValue("[" + lcard + "]");
                deckRef.child("Discard Pile").child("Cards").setValue(1);

                deckRef.child("Discard Pile").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String totalDisc = dataSnapshot.child("Hand").getValue(String.class);
                        Log.d("totalDisc", totalDisc);
                        String realLast = totalDisc.substring(totalDisc.length() - 5, totalDisc.length() - 1);
                        Log.d("realLast", realLast);
                        String PACKAGE_NAME = getApplicationContext().getPackageName();
                        int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+realLast , null, null);
                        discardPile.setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));
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
        ViewGroup view = findViewById(R.id.linear);

        view.removeAllViews();

        handsMap = new HashMap<>();
        array = new boolean[ncards.intValue()];

        for(int n=0; n<ncards.intValue(); n++){

            ImageView iv = new ImageView(this);
            handsMap.put("img" + (n+1), iv);
            array[n] = false;

            //Imageview attributes
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, 230);
            if(n==0) {
                layoutParams.setMarginStart(60);
            } else {
                layoutParams.setMarginStart(-90);
            }
            layoutParams.gravity = Gravity.BOTTOM;
            Objects.requireNonNull(handsMap.get("img" + (n + 1))).setLayoutParams(layoutParams);
            Objects.requireNonNull(handsMap.get("img" + (n + 1))).setImageResource(R.drawable.gray_back);
            Objects.requireNonNull(handsMap.get("img" + (n + 1))).setAdjustViewBounds(true);
            //End Image attributes

            //Get image resource ID dynamically
            String PACKAGE_NAME = getApplicationContext().getPackageName();
            int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+playaHand.get(n) , null, null);
            Objects.requireNonNull(handsMap.get("img" + (n + 1))).setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));

            //Raise card on click
            final int finalN = n;
            Objects.requireNonNull(handsMap.get("img" + (n + 1))).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) Objects.requireNonNull(handsMap.get("img" + (finalN + 1))).getLayoutParams();
                    if(params.gravity== Gravity.TOP){
                        params.gravity = Gravity.BOTTOM;
                        array[finalN] = false;
                    } else {
                        params.gravity = Gravity.TOP;
                        array[finalN] = true;
                    }
                    Objects.requireNonNull(handsMap.get("img" + (finalN + 1))).setLayoutParams(params);
                }
            });

            //Add card attributes
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

                assert order != null;
                if(round!=null && nplayers!=null && order.equals(round%nplayers)){
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
                    assert cards != null;
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

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void Start(View view){
        drawCard();
    }


    public void discard(View view){

        int pickCount = 0;
        int picked = 0;
        int count = 0;

        for(boolean b : array) {
            if(b) {
                picked = count;
                pickCount = pickCount + 1;
            }
            count = count +1;
        }

        switch (pickCount){
            case 0:
                Toast.makeText(this, "No cards picked", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Log.d("picked", String.valueOf(picked));

                final int pick2 = picked;
                final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");

                discRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String pHand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                        String num = pHand.substring(1, pHand.length() - 1);
                        String[] str = num.split(", ");
                        playerHand = new ArrayList<>(Arrays.asList(str));
                        Log.d("playerHand", playerHand.toString());
                        String dCard = playerHand.get(pick2);
                        playerHand.remove(pick2);
                        Log.d("playerHand", playerHand.toString());

                        String dHand = dataSnapshot.child("Discard Pile").child("Hand").getValue(String.class);
                        String dnum = dHand.substring(1, dHand.length() - 1);
                        String[] dstr = dnum.split(", ");
                        ArrayList<String> discHand = new ArrayList<>(Arrays.asList(dstr));
                        Log.d("discHand", discHand.toString());
                        discHand.add(dCard);
                        Log.d("discHand", discHand.toString());

                        discRef.child("Discard Pile").child("Hand").setValue(discHand.toString());
                        discRef.child("Discard Pile").child("Cards").setValue(discHand.size());
                        discRef.child(playerName).child("Hand").setValue(playerHand.toString());
                        discRef.child(playerName).child("Cards").setValue(playerHand.size());

                        drawCards(playerHand, Long.valueOf(str.length - 1));

                        final DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id).child("Round");
                        roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Long round = dataSnapshot.getValue(Long.class);
                                roundRef.setValue(round+1);
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

                break;
            default:
                Toast.makeText(this, "Can't discard more than one card", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void Buy(View view){

    }

}
