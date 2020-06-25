package com.example.pords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    Button btn_start, btn_disc, btn_buy, btn_meld, btn_set;
    LinearLayout linear, Hand1, Hand2, Hand3, Hand4, Hand5, Hand6, Hand7, Hand8, Hand9;
    LinearLayout Hand10, Hand11, Hand12, Hand13, Hand14, Hand15;
    Map<String, Integer> layMap;
    Map<String, ImageView> handsMap, pileMap;
    ImageView discardPile;
    boolean[] array;
    int packed = 0;
    int meldCount = 0;

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
        discardPile = findViewById(R.id.imageViewDiscard);
        Hand1 = findViewById(R.id.Hand1);

        Hand6 = findViewById(R.id.Hand6);

        Hand11 = findViewById(R.id.Hand11);


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

                        btn_set.setEnabled(false);
                        btn_meld.setEnabled(false);
                        btn_disc.setEnabled(false);

                        //Enable DRAW/DISCARD buttons only on player's turn
                        if(round!=null && nplayers!=null && order.equals(round%nplayers)){
                            packed = 0;
                            btn_start.setEnabled(true);
                            if(meldCount>1) {
                                btn_set.setEnabled(true);
                            }
                        } else {
                            btn_start.setEnabled(false);
                        }

                        String hand = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);

                        //Generate hand only on this conditions
                        if(hand!=null && !hand.equals("-")) {
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
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final DatabaseReference pileRef = database.getReference("Ongoing_Matches/" + match_id).child("Players").child("Piles");
        pileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String layHand = ds.getKey();
                        assert layHand != null;
                        String discHand = ds.getValue(String.class);
                        assert discHand != null;
                        String num = discHand.substring(1, discHand.length() - 1);
                        String[] str = num.split(", ");
                        ArrayList disclist = new ArrayList(Arrays.asList(str));

                        drawPile(disclist, Long.valueOf(disclist.size()), layHand);
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
        final DatabaseReference deckRef = database.getReference("Ongoing_Matches/" + matchid).child("Players");
            deckRef.child("Discard Pile").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String totalDisc = dataSnapshot.child("Hand").getValue(String.class);
                    if(totalDisc!=null && !totalDisc.equals("-")){
                        Log.d("totalDisc", totalDisc);
                        String realLast = totalDisc.substring(totalDisc.length() - 5, totalDisc.length() - 1);
                        Log.d("realLast", realLast);
                        String PACKAGE_NAME = getApplicationContext().getPackageName();
                        int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + realLast, null, null);
                        discardPile.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));
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
        view.removeAllViews();

        pileMap = new HashMap<>();

        for(int n=0; n<ncards.intValue(); n++) {

            ImageView iv = new ImageView(this);
            pileMap.put(layName + (n + 1), iv);

            //Imageview attributes
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, 230);
            if (n == 0) {
                layoutParams.setMarginStart(20);
            } else {
                layoutParams.setMarginStart(-90);
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

                String num = hand.substring(1, hand.length() - 1);
                String[] str = num.split(", ");
                playerHand = new ArrayList<>(Arrays.asList(str).subList(0, cards.intValue()));
                Collections.sort(playerHand);
                updateHand(playerHand.toString());

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
                ArrayList<String> decklist = new ArrayList<>(Arrays.asList(str).subList(0, dcards.intValue()));
                String lcard = decklist.get(dcards.intValue()-1);
                decklist.remove(dcards.intValue()-1);

                String hand2 = dataSnapshot.child("Players").child(playerName).child("Hand").getValue(String.class);
                Long dcards2 = dataSnapshot.child("Players").child(playerName).child("Cards").getValue(Long.class);
                assert hand2 != null;
                String num2 = hand2.substring(1,hand2.length()-1);
                String[] str2 = num2.split(", ");
                assert dcards2 != null;
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
        btn_disc.setEnabled(true);
        btn_meld.setEnabled(true);
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
                final int pick2 = picked;
                final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");

                discRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String pHand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                        String num = pHand.substring(1, pHand.length() - 1);
                        String[] str = num.split(", ");
                        playerHand = new ArrayList<>(Arrays.asList(str));
                        String dCard = playerHand.get(pick2);
                        playerHand.remove(pick2);

                        String dHand = dataSnapshot.child("Discard Pile").child("Hand").getValue(String.class);
                        String dnum = dHand.substring(1, dHand.length() - 1);
                        String[] dstr = dnum.split(", ");
                        ArrayList<String> discHand = new ArrayList<>(Arrays.asList(dstr));
                        discHand.add(dCard);

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

                        packed = 0;
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
                Toast.makeText(this, "No cards picked", Toast.LENGTH_SHORT).show();
                break;
            case 1:
            case 2:
                Toast.makeText(this, "Too few cards picked", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                final DatabaseReference discRef = database.getReference("Ongoing_Matches/" + match_id).child("Players");
                discRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String pHand = dataSnapshot.child(playerName).child("Hand").getValue(String.class);
                        String num = pHand.substring(1, pHand.length() - 1);
                        String[] str = num.split(", ");
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

                        if (fq > 1 && mFq != 0 && (wcfq==cWcard || cWcard==0)) {
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

                            if(!dataSnapshot.child("Piles").exists()){
                                layHand = "Hand1";
                            } else {
                                int pileCount = 0;
                                for(DataSnapshot ds : dataSnapshot.child("Piles").getChildren()){
                                    pileCount = pileCount + 1;
                                }
                                layHand = "Hand" + (pileCount+1);
                            }

                            discRef.child("Piles").child(layHand).setValue(disclist.toString());

                            packed = 0;
                            meldCount = meldCount + 1;

                        } else {
                            if (cWcard > 1) {
                                Toast.makeText(MatchActivity.this, "Can't pick more wildcards", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MatchActivity.this, "Three of a kind not accepted", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;
            default:
                Toast.makeText(this, "Number of cards exceeded", Toast.LENGTH_SHORT).show();
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

}
