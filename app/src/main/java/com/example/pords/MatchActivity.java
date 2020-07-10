package com.example.pords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
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
import java.util.Map;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MatchActivity extends AppCompatActivity {

    String match_id, playerName, playerID, role;
    Deck deck;
    FirebaseDatabase database;
    List<String> playerList;
    TextView textView;
    ArrayList<String> playerHand, discHand;
    Button btn_start, btn_disc, btn_buy, btn_meld, btn_set, match_abandon;
    LinearLayout linear, Hand1, Hand2, Hand3, Hand4, Hand5, Hand6, Hand7, Hand8, Hand9;
    LinearLayout Hand10, Hand11, Hand12, Hand13, Hand14, Hand15;
    Map<String, Integer> layMap;
    Map<String, ImageView> handsMap, pileMap;
    ImageView discardPile;
    boolean[] array;
    int packed = 0;

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
        btn_meld = findViewById(R.id.btn_meld);
        linear = findViewById(R.id.linear);
        btn_set = findViewById(R.id.btn_set);
        match_abandon = findViewById(R.id.match_abandon);
        discardPile = findViewById(R.id.imageViewDiscard);

        layMap = new HashMap<>();
        Hand1 = findViewById(R.id.Hand1);
        Hand2 = findViewById(R.id.Hand2);
        Hand3 = findViewById(R.id.Hand3);
        Hand4 = findViewById(R.id.Hand4);
        Hand5 = findViewById(R.id.Hand5);
        Hand6 = findViewById(R.id.Hand6);
        Hand7 = findViewById(R.id.Hand7);
        Hand8 = findViewById(R.id.Hand8);
        Hand9 = findViewById(R.id.Hand9);
        Hand10 = findViewById(R.id.Hand10);
        Hand11 = findViewById(R.id.Hand11);
        Hand12 = findViewById(R.id.Hand12);
        Hand13 = findViewById(R.id.Hand13);
        Hand14 = findViewById(R.id.Hand14);
        Hand15 = findViewById(R.id.Hand15);

        layMap = new HashMap<>();
        layMap.put("Hand1", R.id.Hand1);
        layMap.put("Hand2", R.id.Hand2);
        layMap.put("Hand3", R.id.Hand3);
        layMap.put("Hand4", R.id.Hand4);
        layMap.put("Hand5", R.id.Hand5);
        layMap.put("Hand6", R.id.Hand6);
        layMap.put("Hand7", R.id.Hand7);
        layMap.put("Hand8", R.id.Hand8);
        layMap.put("Hand9", R.id.Hand9);
        layMap.put("Hand10", R.id.Hand10);
        layMap.put("Hand11", R.id.Hand11);
        layMap.put("Hand12", R.id.Hand12);
        layMap.put("Hand13", R.id.Hand13);
        layMap.put("Hand14", R.id.Hand14);
        layMap.put("Hand15", R.id.Hand15);


        //Get intents
        Intent intent = getIntent();
        match_id = intent.getStringExtra("match_id");
        playerName = intent.getStringExtra("playerName");
        playerID = intent.getStringExtra("playerID");
        role = intent.getStringExtra("role");

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString("match", match_id);
        editor.apply();

        //Set username textview
        textView = findViewById(R.id.textView);
        String username = "Usuario: " + playerName;
        textView.setText(username);

        discardPile(match_id);

        match_abandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.remove("match");
                editor.apply();

                final DatabaseReference playersRef = database.getReference("Ongoing_Matches/" + match_id);

                playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Long size = dataSnapshot.child("Size").getValue(Long.class);
                        Long count = dataSnapshot.child("Count").getValue(Long.class);

                        //Take player hand
                        String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                        Long order = dataSnapshot.child("Players").child(playerName).child("Order").getValue(Long.class);
                        assert hand != null;
                        String num = hand.substring(1,hand.length()-1);
                        String[] str = num.split(", ");
                        playerHand = new ArrayList<>(Arrays.asList(str));

                        //Pass hand to deck and shuffle
                        String dhand = dataSnapshot.child("Players").child("Deck").child("Hand").getValue(String.class);
                        assert dhand != null;
                        String dnum = dhand.substring(1,dhand.length()-1);
                        String[] dstr = dnum.split(", ");
                        ArrayList<String> deckHand = new ArrayList<>(Arrays.asList(dstr));

                        deckHand.addAll(playerHand);
                        Collections.shuffle(deckHand);

                        playersRef.child("Players").child("Deck").child("Hand").setValue(deckHand.toString());
                        playersRef.child("Players").child("Deck").child("Cards").setValue(deckHand.size());

                        //Delete player data
                        playersRef.child("Players").child(playerName).removeValue();

                        //Find order gap
                        ArrayList<Long> orderz = new ArrayList<>();

                        for(DataSnapshot ds : dataSnapshot.child("Players").getChildren()){
                            if(ds.child("Order").exists()) {
                                orderz.add(ds.child("Order").getValue(Long.class));
                            }
                        }

                        int n = size.intValue();
                        int total = n*(n-1)/2;

                        Long[] longArray = new Long[n];
                        longArray = orderz.toArray(longArray);

                        for(int i = 0; i < n; i++) {
                            total -= longArray[i];
                        }

                        //Change orders
                        for(DataSnapshot ds : dataSnapshot.child("Players").getChildren()){
                            Long new_order = ds.child("Order").getValue(Long.class);
                            if(ds.child("Order").exists() && new_order>total) {
                                String playName = ds.getKey();
                                playersRef.child("Players").child(playName).child("Order").setValue(new_order-1);
                            }
                        }

                        playersRef.child("Size").setValue(size-1);
                        playersRef.child("Count").setValue(count-1);

                        final DatabaseReference userRef = database.getReference("Users/" + playerID);
                        userRef.child("In_Match").removeValue();



                        playersRef.child("Round").setValue(total);

                        Intent intent1 = new Intent(MatchActivity.this, LobbyActivity.class);
                        startActivity(intent1);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });


            }
        });

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

                        btn_set.setEnabled(false);
                        btn_set.setAlpha(0.25f);
                        btn_meld.setEnabled(false);
                        btn_meld.setAlpha(0.25f);
                        btn_disc.setEnabled(false);
                        btn_disc.setAlpha(0.25f);
                        btn_buy.setEnabled(true);
                        btn_buy.setAlpha(1f);

                        //Enable DRAW/DISCARD buttons only on player's turn
                        if(round!=null && nplayers!=null && order!=null && order.equals(round%nplayers)){
                            packed = 0;
                            btn_start.setEnabled(true);
                            btn_start.setAlpha(1f);
                        } else {
                            btn_start.setEnabled(false);
                            btn_start.setAlpha(0.25f);
                            btn_set.setEnabled(false);
                            btn_set.setAlpha(0.25f);
                        }

                        String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                        assert hand != null;
                        String num = hand.substring(1,hand.length()-1);
                        String[] str = num.split(", ");
                        playerHand = new ArrayList<>(Arrays.asList(str));

                        //Generate hand only on this conditions
                        if(playerHand!=null && !playerHand.toString().equals("-")) {
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

        //Listen values on Piles change
        final DatabaseReference pileRef = database.getReference("Ongoing_Matches/" + match_id).child("Players").child("Piles");
        pileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        String layHand = ds.getKey();
                        assert layHand != null;

                        String hand = ds.getValue(String.class);
                        assert hand != null;
                        String num = hand.substring(1,hand.length()-1);
                        String[] str = num.split(", ");
                        ArrayList<String> disclist = new ArrayList<>(Arrays.asList(str));

                        drawPile(disclist, (long) disclist.size(), layHand);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Match winner
        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");
        handRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if(!ds.getKey().equals("Discard Pile") && !ds.getKey().equals("Deck")) {

                            Long ncards = ds.child("Cards").getValue(Long.class);

                            if (ncards != null && ncards == 0) {

                                score();

                                String winner = ds.getKey();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MatchActivity.this);
                                builder.setTitle("El Ganador es:");
                                builder.setMessage(winner);
                                builder.setPositiveButton("Aceptar", null);

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void discardPile(String matchid){
        //Get Discard Pile
        final DatabaseReference deckRef = database.getReference("Ongoing_Matches/" + matchid).child("Players").child("Discard Pile").child("Hand");
        deckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String dhand = dataSnapshot.getValue(String.class);

                if(dhand!=null && !dhand.equals("-")) {

                    String dnum = dhand.substring(1, dhand.length() - 1);
                    String[] dstr = dnum.split(", ");
                    discHand = new ArrayList<>(Arrays.asList(dstr));

                    String totalDisc = discHand.toString();
                    if (totalDisc != null && !totalDisc.equals("-") && !totalDisc.equals("[]")) {
                        discardPile.setVisibility(View.VISIBLE);
                        String PACKAGE_NAME = getApplicationContext().getPackageName();
                        int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + discHand.get(discHand.size() - 1), null, null);
                        discardPile.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));
                    } else if(totalDisc.equals("[]")){
                        discardPile.setVisibility(View.GONE);
                    }

                }

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
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, height);
            if (n == 0) {
                int mg = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                layoutParams.setMarginStart(mg);
            } else {
                int mg2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -35, getResources().getDisplayMetrics());
                layoutParams.setMarginStart(mg2);
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
                        packed = packed - 1;
                    } else {
                        params.gravity = Gravity.TOP;
                        array[finalN] = true;
                        packed = packed + 1;
                    }
                    Objects.requireNonNull(handsMap.get("img" + (finalN + 1))).setLayoutParams(params);
                }
            });

            //Add card attributes
            view.addView(handsMap.get("img" + (n+1)));
        }
    }

    public void drawPile(final ArrayList<String> pHand, final Long ncards, final String layName) {

        ViewGroup view = findViewById(layMap.get(layName));
        view.setVisibility(View.VISIBLE);
        view.removeAllViews();

        pileMap = new HashMap<>();

        for(int n=0; n<ncards.intValue(); n++) {

            ImageView iv = new ImageView(this);
            pileMap.put(layName + (n + 1), iv);

            //Imageview attributes
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, height);
            if (n == 0) {
                int mg = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                layoutParams.setMarginStart(mg);
            } else {
                int mg2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -45, getResources().getDisplayMetrics());
                layoutParams.setMarginStart(mg2);
            }
            layoutParams.gravity = Gravity.BOTTOM;
            Objects.requireNonNull(pileMap.get(layName + (n + 1))).setLayoutParams(layoutParams);
            Objects.requireNonNull(pileMap.get(layName + (n + 1))).setImageResource(R.drawable.gray_back);
            Objects.requireNonNull(pileMap.get(layName + (n + 1))).setAdjustViewBounds(true);
            //End Image attributes

            //Get image resource ID dynamically
            String PACKAGE_NAME = getApplicationContext().getPackageName();
            int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + pHand.get(n), null, null);
            Objects.requireNonNull(pileMap.get(layName + (n + 1))).setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));

            view.addView(pileMap.get(layName + (n+1)));
        }
    }


    public void Sort(View views){

        DatabaseReference roundRef = database.getReference("Ongoing_Matches/" + match_id);
        roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                Long cards = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                assert hand != null;
                assert cards != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");
                playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));

                Collections.sort(playerHand);
                updateHand(playerHand);

                Long round = dataSnapshot.child("Round").getValue(Long.class);
                Long nplayers = dataSnapshot.child("Size").getValue(Long.class);
                Long order = dataSnapshot.child("Players").child(playerName).child("Order").getValue(Long.class);

                assert order != null;
                if(round!=null && nplayers!=null && order.equals(round%nplayers)){
                    btn_start.setEnabled(true);
                    btn_start.setAlpha(1f);
                } else {
                    btn_start.setEnabled(false);
                    btn_start.setAlpha(0.25f);
                }

                if(playerHand.toString()!=null && !playerHand.toString().equals("-")) {
                    drawCards(playerHand, cards);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void updateHand(ArrayList<String> vHand){
        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id).child("Players").child(playerName).child("Hand");
        handRef.setValue(vHand.toString());
    }

    public void drawCard(){
        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id);

        handRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String hand = dataSnapshot.child("Players").child("Deck").child("Hand").getValue(String.class);
                Long dcards = dataSnapshot.child("Players").child("Deck").child("Cards").getValue(Long.class);
                assert hand != null;
                assert dcards != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");
                ArrayList<String> decklist = new ArrayList<>(Arrays.asList(str).subList(0, dcards.intValue()));

                String lcard = decklist.get(dcards.intValue()-1);
                decklist.remove(dcards.intValue()-1);

                String hand2 = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                Long dcards2 = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                assert hand2 != null;
                assert dcards2 != null;
                String num2 = hand2.substring(1,hand2.length()-1);
                String[] str2 = num2.split(", ");
                playerHand = new ArrayList<>(Arrays.asList(str2).subList(0, dcards2.intValue()));

                playerHand.add(lcard);

                handRef.child("Players").child("Deck").child("Hand").setValue(decklist.toString());
                handRef.child("Players").child("Deck").child("Cards").setValue(dcards.intValue()-1);
                handRef.child("Players").child(playerName).child("Cards").setValue(playerHand.size());
                handRef.child("Players").child(playerName).child("Hand").setValue(playerHand.toString());

                drawCards(playerHand, Long.valueOf(playerHand.size()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void Start(View view){
        drawCard();
        btn_start.setEnabled(false);
        btn_start.setAlpha(0.25f);
        btn_disc.setEnabled(true);
        btn_disc.setAlpha(1f);

        final DatabaseReference meldRef = database.getReference("Ongoing_Matches/" + match_id).child("Players").child(playerName).child("Meld");
        meldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long meld = dataSnapshot.getValue(Long.class);
                if(meld!=null && meld>1){
                    btn_set.setEnabled(true);
                    btn_set.setAlpha(1f);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btn_meld.setEnabled(true);
        btn_meld.setAlpha(1f);

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
                Toast.makeText(this, "No se seleccionaron cartas", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                final int pick2 = picked;
                final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");

                discRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String hand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                        Long cards = dataSnapshot.child(playerName).child("Cards").getValue(Long.class);
                        assert hand != null;
                        assert cards != null;
                        String num = hand.substring(1,hand.length()-1);
                        String[] str = num.split(", ");
                        playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));

                        String hand2 = dataSnapshot.child("Discard Pile").child("Hand").getValue(String.class);
                        Long cards2 = dataSnapshot.child("Discard Pile").child("Cards").getValue(Long.class);
                        assert hand2 != null;
                        assert cards2 != null;
                        String num2 = hand2.substring(1,hand2.length()-1);
                        String[] str2 = num2.split(", ");
                        discHand = new ArrayList<>(Arrays.asList(str2).subList(0, cards2.intValue()));

                        String dCard = playerHand.get(pick2);
                        playerHand.remove(pick2);

                        discHand.add(dCard);

                        discRef.child("Discard Pile").child("Hand").setValue(discHand.toString());
                        discRef.child("Discard Pile").child("Cards").setValue(discHand.size());
                        discRef.child(playerName).child("Hand").setValue(playerHand.toString());
                        discRef.child(playerName).child("Cards").setValue(playerHand.size());

                        if(playerHand.size()==0){
                            drawCards(playerHand, 0L);
                        } else {
                            drawCards(playerHand, Long.valueOf(playerHand.size()) - 1);
                        }

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

                        packed = 0;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                break;
            default:
                Toast.makeText(this, "No se puede descartas más de una carta", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void Buy(View view){

        final DatabaseReference buyRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");
        buyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Long purch = dataSnapshot.child(playerName).child("Purchases").getValue(Long.class);

                if(purch<=7) {

                    String hand = dataSnapshot.child("Discard Pile").child("Hand").getValue(String.class);
                    assert hand != null;
                    String num = hand.substring(1,hand.length()-1);
                    String[] str = num.split(", ");
                    ArrayList<String> discHand = new ArrayList<>(Arrays.asList(str));

                    String hand2 = dataSnapshot.child("Deck").child("Hand").getValue(String.class);
                    assert hand2 != null;
                    String num2 = hand2.substring(1,hand2.length()-1);
                    String[] str2 = num2.split(", ");
                    ArrayList<String> deckHand = new ArrayList<>(Arrays.asList(str2));

                    String lastCard = discHand.get(discHand.size() - 1);
                    discHand.remove(discHand.size() - 1);

                    String dlastCard = deckHand.get(deckHand.size() - 1);
                    deckHand.remove(deckHand.size() - 1);

                    String hand3 = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                    assert hand3 != null;
                    String num3 = hand3.substring(1,hand3.length()-1);
                    String[] str3 = num3.split(", ");
                    playerHand = new ArrayList<>(Arrays.asList(str3));

                    playerHand.add(lastCard);
                    playerHand.add(dlastCard);

                    buyRef.child("Discard Pile").child("Hand").setValue(discHand.toString());
                    buyRef.child("Discard Pile").child("Cards").setValue(discHand.size());
                    buyRef.child("Deck").child("Hand").setValue(deckHand.toString());
                    buyRef.child("Deck").child("Cards").setValue(deckHand.size());
                    buyRef.child(playerName).child("Hand").setValue(playerHand.toString());
                    buyRef.child(playerName).child("Cards").setValue(playerHand.size());
                    buyRef.child(playerName).child("Purchases").setValue(purch+1);

                    drawCards(playerHand, (long) playerHand.size());

                } else {
                    Toast.makeText(MatchActivity.this, "Ha alcanzado el límite de compras", Toast.LENGTH_SHORT).show();
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btn_buy.setEnabled(false);
        btn_buy.setAlpha(0.25f);

    }

    public void Set(View view){

        int pickCount = 0;
        final int[] picked = new int[packed];
        int count = 0;

        for(boolean b : array) {
            if(b) {
                picked[pickCount] = count;
                pickCount = pickCount + 1;
            }
            count = count +1;
        }


        final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");
        discRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String hand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                assert hand != null;
                String num = hand.substring(1,hand.length()-1);
                String[] str = num.split(", ");

                String[] trio = new String[packed];
                for(int w=0 ; w<packed ; w++){
                    trio[w] = str[picked[w]];
                }

                int cTot = 0;
                int cReal = 0;
                int cWcard = 0;
                int[] cRank = new int[packed];

                for (String x : trio) {
                    if (x.substring(2).equals("02") || x.substring(0, 2).equals("jk")) {
                        cWcard = cWcard + 1;
                    } else {
                        cRank[cTot] = Integer.parseInt(x.substring(2));
                        cReal = cReal + 1;
                    }
                    cTot = cTot + 1;
                }

                int mFq = mostFrequent(cRank, packed).get("res");
                int fq;

                if(cReal==0){
                    fq = 0;
                } else {
                    fq = mostFrequent(cRank, packed).get("max count");
                }


                if ((fq + cWcard)==packed) {
                    ArrayList<String> handlist = new ArrayList<>(Arrays.asList(str));
                    for(int a=0; a<packed; a++) {
                        handlist.remove(picked[a] - a);
                    }

                    discRef.child(playerName).child("Hand").setValue(handlist.toString());
                    discRef.child(playerName).child("Cards").setValue(str.length - packed);
                    drawCards(handlist, Long.valueOf(str.length - packed));

                    final Map<String, String[]> keyMap = new HashMap<>();
                    int k = 0;

                    for(DataSnapshot ds : dataSnapshot.child("Piles").getChildren()){

                        String phand = ds.getValue(String.class);
                        assert phand != null;
                        String pnum = phand.substring(1,phand.length()-1);
                        String[] pstr = pnum.split(", ");
                        ArrayList<String> arrayPile = new ArrayList<>();
                        for(String st : pstr){
                            arrayPile.add(st.substring(2));
                        }
                        keyMap.put("Hand" + k, arrayPile.toString().substring(1,arrayPile.toString().length()-1).split(", "));
                        k++;
                    }

                    ArrayList<Integer> fqs = new ArrayList<>();
                    ArrayList<Integer> psz = new ArrayList<>();

                    for(int h=0; h<k; h++){
                        int[] imp = new int[keyMap.get("Hand" + h).length];
                        for(int l=0; l<keyMap.get("Hand" + h).length; l++){
                            imp[l] = Integer.parseInt(keyMap.get("Hand" + h)[l]);
                        }
                        fqs.add(mostFrequent(imp, imp.length).get("res"));
                        psz.add(imp.length);
                    }

                    int layHand;

                    if(fq==0){
                        layHand = psz.indexOf(Collections.min(psz));
                    } else {
                        layHand = fqs.indexOf(mFq);
                    }

                    String tphand = dataSnapshot.child("Piles").child("Hand" + (layHand+1)).getValue(String.class);
                    assert tphand != null;
                    String tpnum = tphand.substring(1,tphand.length()-1);
                    String[] tpstr = tpnum.split(", ");

                    ArrayList<String> topilelist = new ArrayList<>(Arrays.asList(tpstr));

                    topilelist.addAll(Arrays.asList(trio).subList(0, packed));

                    discRef.child("Piles").child("Hand" + (layHand+1)).setValue(topilelist.toString());

                    packed = 0;

                } else {
                    Toast.makeText(MatchActivity.this, "No se pueden acomodar estas cartas", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void Meld(View view){

        int pickCount = 0;
        final int[] picked = new int[packed];
        int count = 0;

        for(boolean b : array) {
            if(b) {
                picked[pickCount] = count;
                pickCount = pickCount + 1;
            }
            count = count +1;
        }

        switch (packed){
            case 0:
                Toast.makeText(this, "No se seleccionaron cartas", Toast.LENGTH_SHORT).show();
                break;
            case 1:
            case 2:
                Toast.makeText(this, "No se seleccionaron suficientas cartas", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");
                discRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Long meld = dataSnapshot.child(playerName).child("Meld").getValue(Long.class);

                        String hand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                        assert hand != null;
                        String num = hand.substring(1,hand.length()-1);
                        String[] str = num.split(", ");

                        if(str.length!=3) {

                            String[] trio = new String[3];
                            trio[0] = str[picked[0]];
                            trio[1] = str[picked[1]];
                            trio[2] = str[picked[2]];

                            int cReal = 0;
                            int cWcard = 0;
                            int[] cRank = new int[3];

                            for (String x : trio) {
                                if (x.substring(2).equals("02") || x.substring(0, 2).equals("jk")) {
                                    cWcard = cWcard + 1;
                                } else {
                                    cRank[cReal] = Integer.parseInt(x.substring(2));
                                }
                                cReal = cReal + 1;
                            }

                            int mFq = mostFrequent(cRank, 3).get("res");
                            int fq = mostFrequent(cRank, 3).get("max count");
                            int wcFq = leastFrequent(cRank, 3).get("res");
                            int wcfq = leastFrequent(cRank, 3).get("max count");

                            if (fq > 1 && mFq != 0 && (wcfq == cWcard || cWcard == 0)) {
                                ArrayList<String> handlist = new ArrayList<>(Arrays.asList(str));
                                handlist.remove(picked[0]);
                                handlist.remove(picked[1] - 1);
                                handlist.remove(picked[2] - 2);
                                discRef.child(playerName).child("Hand").setValue(handlist.toString());
                                discRef.child(playerName).child("Cards").setValue(str.length - 3);
                                drawCards(handlist, Long.valueOf(str.length - 3));

                                ArrayList<String> disclist = new ArrayList<>();
                                disclist.add(trio[0]);
                                disclist.add(trio[1]);
                                disclist.add(trio[2]);

                                String layHand;

                                if (!dataSnapshot.child("Piles").exists()) {
                                    layHand = "Hand1";
                                } else {
                                    int pileCount = 0;
                                    for (DataSnapshot ds : dataSnapshot.child("Piles").getChildren()) {
                                        pileCount = pileCount + 1;
                                    }
                                    layHand = "Hand" + (pileCount + 1);
                                }

                                discRef.child("Piles").child(layHand).setValue(disclist.toString());

                                packed = 0;

                                discRef.child(playerName).child("Meld").setValue(meld + 1);

                            } else {
                                if (cWcard > 1) {
                                    Toast.makeText(MatchActivity.this, "No se pueden usar más comodines", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MatchActivity.this, "Trío no aceptado", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            Toast.makeText(MatchActivity.this, "Debe tener una carta para poder descartar", Toast.LENGTH_SHORT).show();
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;
            default:
                Toast.makeText(this, "Número de cartas excedido", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public Map<String, Integer> mostFrequent(int[] arr, int n) {

        // Sort the array
        Arrays.sort(arr);

        Map<String, Integer> map = new HashMap<>();

        // find the max frequency using linear
        // traversal
        int max_count = 1;
        int res = arr[0];
        int curr_count = 1;

        for (int i = 1; i < n; i++)
        {
            if (arr[i] == arr[i - 1])
                curr_count++;
            else
            {
                if (curr_count > max_count)
                {
                    max_count = curr_count;
                    res = arr[i - 1];
                }
                curr_count = 1;
            }
        }

        // If last element is most frequent
        if (curr_count > max_count)
        {
            max_count = curr_count;
            res = arr[n - 1];
        }

        map.put("res", res);
        map.put("max count", max_count);

        return map;
    }


    public Map<String, Integer> leastFrequent(int[] arr, int n) {

        // Sort the array
        Arrays.sort(arr);

        Map<String, Integer> lmap = new HashMap<>();

        // find the min frequency using
        // linear traversal
        int min_count = n+1, res = -1;
        int curr_count = 1;

        for (int i = 1; i < n; i++) {
            if (arr[i] == arr[i - 1])
                curr_count++;
            else {
                if (curr_count < min_count) {
                    min_count = curr_count;
                    res = arr[i - 1];
                }

                curr_count = 1;
            }
        }

        // If last element is least frequent
        if (curr_count < min_count)
        {
            min_count = curr_count;
            res = arr[n - 1];
        }

        lmap.put("res", res);
        lmap.put("max count", min_count);

        return lmap;
    }

    public void score(){

        final DatabaseReference handRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");

        handRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(!ds.getKey().equals("Discard Pile") && !ds.getKey().equals("Deck"))  {

                        final String hand = ds.child("Hand").getValue(String.class);

                        if(hand!=null && hand.equals("[]")){
                            handRef.child(ds.getKey()).child("Score").setValue(0);
                        } else if(hand!=null){
                            String num = hand.substring(1, hand.length() - 1);
                            String[] str = num.split(", ");

                            int score = 0;

                            for(String sc : str){

                                if(sc.equals("jk01") || sc.equals("jk02")){
                                    score += 50;
                                } else if(sc.equals("ah01") || sc.equals("bd01") || sc.equals("cc01") || sc.equals("ds01")){
                                    score += 20;
                                } else if(sc.equals("ah02") || sc.equals("bd02") || sc.equals("cc02") || sc.equals("ds02")){
                                    score += 30;
                                } else if(Integer.parseInt(sc.substring(2))>2 && Integer.parseInt(sc.substring(2))<8){
                                    score += 5;
                                } else {
                                    score += 10;
                                }
                            }

                            handRef.child(ds.getKey()).child("Score").setValue((long) score);

                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

}
