package com.example.pords;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.ValueEventListener;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText reg_name, reg_lname, reg_user, reg_pass, reg_email, reg_phone;
    TextView reg_date;
    Button reg_register, reg_cancel;
    FirebaseDatabase database;
    private DatePickerDialog dpd;
    private DatePickerDialog.OnDateSetListener setListener;
    Calendar c;
    String bmonth, bday, adLine, key;
    private FusedLocationProviderClient fusedLocationProviderClient;
    boolean userExists = false;
    boolean emailExists = false;
    boolean phoneExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Declare variables
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        } else {
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }

        reg_name = findViewById(R.id.reg_name);
        reg_lname = findViewById(R.id.reg_lname);
        reg_user = findViewById(R.id.reg_user);
        reg_pass = findViewById(R.id.reg_pass);
        reg_email = findViewById(R.id.reg_email);
        reg_phone = findViewById(R.id.reg_phone);
        reg_date = findViewById(R.id.reg_date);
        reg_register = findViewById(R.id.reg_register);
        reg_cancel = findViewById(R.id.reg_cancel);

        database = FirebaseDatabase.getInstance();

        //Date Picker Dialog
        c = Calendar.getInstance();
        c.add(Calendar.YEAR,-18);
        final int tday = c.get(Calendar.DAY_OF_MONTH);
        final int tmonth = c.get(Calendar.MONTH);
        final int tyear = c.get(Calendar.YEAR);

        reg_date.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                dpd = new DatePickerDialog(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListener, tyear, tmonth, tday);
                Objects.requireNonNull(dpd.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dpd.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(month<9){
                    bmonth = "0" + (month+1);
                } else {
                    bmonth = String.valueOf(month+1);
                }
                if(dayOfMonth<10){
                    bday = "0" + (dayOfMonth);
                } else {
                    bday = String.valueOf(dayOfMonth);
                }
                String date = bday + "/" + bmonth + "/" + year;
                reg_date.setText(date);
            }
        };

        //Dismiss registration click
        reg_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                moveTaskToBack(false);
            }
        });

        //Register click
        reg_register.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                final DatabaseReference userRef = database.getReference("Users");
                String name = reg_name.getText().toString();
                final String lname = reg_lname.getText().toString().trim();
                final String user = reg_user.getText().toString().trim();
                final String pass = reg_pass.getText().toString().trim();
                final String email = reg_email.getText().toString().trim();
                final String date = reg_date.getText().toString().trim();
                final String phone = reg_phone.getText().toString().trim();
                String time = OffsetDateTime.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                final String location = getLocation();

                boolean failFlag = false;

                if(name.length() == 0){
                    failFlag = true;
                }
                if(lname.length() == 0){
                    failFlag = true;
                }
                if(user.length() == 0){
                    failFlag = true;
                }
                if(pass.length() == 0){
                    failFlag = true;
                }
                if(email.length() == 0){
                    failFlag = true;
                }
                if(date.length() == 0){
                    failFlag = true;
                }
                if(phone.length() == 0 ){
                    failFlag = true;
                }

                if(!failFlag) {

                    //Check if user exists
                    userRef.orderByChild("User").equalTo(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                userExists = true;
                            } else {
                                Toast.makeText(RegisterActivity.this, "User already registered", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    //Check if email exists
                    userRef.orderByChild("Email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                emailExists = true;
                            } else {
                                Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    //Check if phone exists
                    userRef.orderByChild("Phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                phoneExists = true;
                            } else {
                                Toast.makeText(RegisterActivity.this, "Phone already registered", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    //If username, mail and phone not registered
                    if (userExists && emailExists && phoneExists) {
                        key = userRef.push().getKey();
                        assert key != null;
                        final DatabaseReference usersData = userRef.child(key);
                        registerUser(usersData, name, lname, user, pass, email, date, phone, time, location);
                        Toast.makeText(RegisterActivity.this, "User successfully registered", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Location method
    public String getLocation() {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location != null){
                    try {
                        Geocoder geocoder = new Geocoder(RegisterActivity.this, Locale.getDefault());
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

    private void registerUser(DatabaseReference usersData, String name, String lname, String user,
                              String pass, String email, String date, String phone, String time, String location){

        usersData.child("Name").setValue(name);
        usersData.child("Last Name").setValue(lname);
        usersData.child("User").setValue(user);
        usersData.child("Password").setValue(pass);
        usersData.child("Email").setValue(email);
        usersData.child("Birth").setValue(date);
        usersData.child("Phone").setValue(phone);
        usersData.child("Time").setValue(time);
        usersData.child("Location").setValue(location);

        Toast.makeText(RegisterActivity.this, "User successfully registered", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        moveTaskToBack(false);
    }


}
