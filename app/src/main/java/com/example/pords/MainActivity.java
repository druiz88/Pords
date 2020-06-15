package com.example.pords;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    EditText et_user, et_pass;
    Button btn_login, btn_reg;
    FirebaseDatabase database;
    String key, adLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declare variables
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
        database = FirebaseDatabase.getInstance();
        et_user = findViewById(R.id.log_user);
        et_pass = findViewById(R.id.log_pass);
        btn_login = findViewById(R.id.btn_login);
        btn_reg = findViewById(R.id.btn_reg);

        //Login button click
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String etuser = et_user.getText().toString();
                final String etpass = et_pass.getText().toString();

                //Compare password method
                DatabaseReference usersRef = database.getReference("Users");
                //Get user key
                usersRef.orderByChild("User").equalTo(etuser).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            key = Objects.requireNonNull(child.getKey());
                        }
                        if(key != null) {
                            //Use key to pair password
                            String pass = dataSnapshot.child(key).child("Password").getValue(String.class);
                            assert pass != null;
                            if (pass.equals(etpass)) {
                                loginUser(key, etuser);
                                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
                                intent.putExtra("player", etuser);
                                intent.putExtra("key", key);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User not registered", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });



            }
        });

        //Register button click
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser(String key, String user){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        final String starts = sdf.format(new Date());
        final String location = getLocation();

        DatabaseReference logRef = database.getReference("Login");
        String logkey = logRef.push().getKey();
        assert logkey != null;

        logRef.child(logkey).child("User").setValue(user);
        logRef.child(logkey).child("Starts").setValue(starts);
        logRef.child(logkey).child("Ends").setValue("-");
        logRef.child(logkey).child("Location").setValue(location);
    }

    //Location method
    public String getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location != null){
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        adLine = String.valueOf(addresses.get(0).getAddressLine(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return adLine;
    }

}
