package com.example.pords;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;


public class ListAdapter extends ArrayAdapter<String> implements View.OnClickListener {

    private List<String> users;
    private LayoutInflater mInflater;
    private int mViewResourceId;
    private FloatingActionButton itemfab, itemfab2, itemfab3;
    private Context context;
    private HttpURLConnection con;
    private BufferedOutputStream os;
    FirebaseDatabase database;
    String line = null;
    String result = null;
    String query, playerName;
    private String matched;

    public ListAdapter(Context context, int textViewResourceId, List<String> userList, String playa) {
        super(context, textViewResourceId);
        this.context = context;
        this.users = userList;
        this.playerName = playa;
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




        return view;
    }

    @Override
    public void onClick(View v) {

    }
}
