package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class ActivitySettings extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private ImageButton connect;
    private ImageButton info;
    private EditText wifi;
    private EditText password;
    private Button cancel;
    private Button save;

    //defining privates
    private FirebaseUser user;
    private boolean isConnecting;
    private boolean isConnected;
    private boolean isInStandBy;
    private boolean isOtherConnected;
    private String oldWifi;
    private String oldPassword;
    private String deviceID;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        connect = findViewById(R.id.establishConnection);
        info = findViewById(R.id.openInfo);
        wifi = findViewById(R.id.editTextWifiName);
        password = findViewById(R.id.editTextWifiPassword);

        TimerTask timerProgramFinished = new ProgramFinished();
        TimerTask timerCameBack = new CameBack();
        TimerTask timerNoInternet = new NoInternet();
        TimerTask timerConnected = new Connected();
        //running timer task as daemon thread
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerProgramFinished, 0, 20000);
        timer.scheduleAtFixedRate(timerCameBack, 0, 20000);
        timer.scheduleAtFixedRate(timerNoInternet, 0, 12000);
        timer.scheduleAtFixedRate(timerConnected, 0, 1000);

        Intent connected = getIntent();

        isConnecting = connected.getBooleanExtra("CONNECT", false);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting current user authentification data
        user = firebaseAuth.getCurrentUser();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                isInStandBy = user.isStandBy();
                oldWifi = "" + dataSnapshot.child("ssid").getValue();
                oldPassword = "" + dataSnapshot.child("password").getValue();
                wifi.setText(oldWifi);
                password.setText(oldPassword);
                deviceID = user.getDeviceID();
                databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("connected").exists()) {
                            isOtherConnected = (boolean) dataSnapshot.child("connected").getValue();
                        }
                        if (!isInStandBy && isOtherConnected) {
                            isConnected = true;
                            isOtherConnected = false;
                        } else {
                            isConnected = false;
                        }

                        if (isConnected || isConnecting) {
                            connect.setBackgroundResource(R.drawable.radio_button_pressed);
                        } else {
                            connect.setBackgroundResource(R.drawable.radio_button_unpressed);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    if (!isConnected && !isConnecting) {
                        if (!isOtherConnected) {
                            Intent assurance = new Intent(ActivitySettings.this, PopupAsurranceConnect.class);
                            assurance.putExtra("CONNECT", true);
                            startActivity(assurance);
                        } else {
                            displayMessage(getString(R.string.PlatformInUse), 4000);
                        }
                    } else {
                        isConnected = false;
                        connect.setBackgroundResource(R.drawable.radio_button_unpressed);
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    startActivity(new Intent(getApplicationContext(), PopupInfo.class));
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        cancel = findViewById(R.id.cancelSettings);
        save = findViewById(R.id.saveSettings);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                finish();
                startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.child("wait").exists()) {
                                if (!dataSnapshot.hasChild("program")) {
                                    if (!isOtherConnected) {
                                        databaseUsers.child(user.getUid()).child("standBy").setValue(false);
                                        if (isConnecting) {
                                            databaseDevices.child(deviceID).child("connected").setValue(true);
                                            String newWifi = wifi.getText().toString();
                                            String newPassword = password.getText().toString();
                                            boolean modified = false;

                                            if (!TextUtils.isEmpty(newWifi) && !newWifi.equals(oldWifi)) {
                                                databaseUsers.child(user.getUid()).child("ssid").setValue(newWifi);
                                                modified = true;
                                            }
                                            if (!TextUtils.isEmpty(newPassword) && !newPassword.equals(oldPassword)) {
                                                databaseUsers.child(user.getUid()).child("password").setValue(newPassword);
                                                modified = true;
                                            }

                                            if (modified) {
                                                displayMessage(getString(R.string.NetworkModified), 2000);
                                                databaseDevices.child(deviceID).child("ssid").setValue(newWifi);
                                                databaseDevices.child(deviceID).child("password").setValue(newPassword);
                                            } else {
                                                    databaseDevices.child(deviceID).child("ssid").setValue(oldWifi);
                                                    databaseDevices.child(deviceID).child("password").setValue(oldPassword);
                                            }

                                            databaseDevices.child(deviceID).child("otherNetwork").setValue(true);
                                            displayMessage(getString(R.string.Connected), 3000);
                                        } else {
                                            if (isConnected) {
                                                String newWifi = wifi.getText().toString();
                                                String newPassword = password.getText().toString();
                                                boolean modified = false;

                                                if (!TextUtils.isEmpty(newWifi) && !newWifi.equals(oldWifi)) {
                                                    databaseUsers.child(user.getUid()).child("ssid").setValue(newWifi);
                                                    modified = true;
                                                }
                                                if (!TextUtils.isEmpty(newPassword) && !newPassword.equals(oldPassword)) {
                                                    databaseUsers.child(user.getUid()).child("password").setValue(newPassword);
                                                    modified = true;
                                                }

                                                if (modified) {
                                                    displayMessage(getString(R.string.NetworkModified), 2000);
                                                    databaseDevices.child(deviceID).child("ssid").setValue(newWifi);
                                                    databaseDevices.child(deviceID).child("password").setValue(newPassword);
                                                    databaseDevices.child(deviceID).child("otherNetwork").setValue(true);
                                                }
                                            } else {
                                                displayMessage(getString(R.string.Disconnected), 3000);

                                                databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        databaseDevices.child(deviceID).child("connected").removeValue();
                                                        databaseDevices.child(deviceID).child("default").child("ssid").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                databaseDevices.child(deviceID).child("ssid").setValue(dataSnapshot.getValue());
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                        databaseDevices.child(deviceID).child("default").child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                databaseDevices.child(deviceID).child("password").setValue(dataSnapshot.getValue());
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
                                                databaseDevices.child(deviceID).child("otherNetwork").removeValue();
                                            }
                                        }
                                        timer.cancel();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                                    } else {
                                        displayMessage(getString(R.string.PlatformInUseError), 4000);
                                    }
                                } else {
                                    displayMessage(getString(R.string.ChangeSettingsError), 3000);
                                }
                            } else {
                                displayMessage(getString(R.string.Conecting), 3000);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivitySettings.this, message, Toast.LENGTH_LONG);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    public class ProgramFinished extends TimerTask {

        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("program").exists() && !isInStandBy) {
                            if (dataSnapshot.child("program").hasChild("wait") && dataSnapshot.child("program").hasChild("alertOn")) {
                                boolean wait = (boolean) dataSnapshot.child("program").child("wait").getValue();
                                boolean  alertOn = (boolean) dataSnapshot.child("program").child("alertOn").getValue();
                                if (wait && alertOn) {
                                    timer.cancel();
                                    startActivity(new Intent(getApplicationContext(), PopupProgramFinished.class));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }


    public class CameBack extends TimerTask {
        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("cameBackFrom").exists() && !isInStandBy) {
                            timer.cancel();
                            startActivity(new Intent(getApplicationContext(), PopupCameBack.class));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public class NoInternet extends TimerTask {
        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("wait").exists()) {
                            timer.cancel();
                            startActivity(new Intent(getApplicationContext(), PopupNoInternet.class));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public class Connected extends TimerTask {
        @Override
        public void run() {
            if (deviceID != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("connected").exists()) {
                            databaseUsers.child(user.getUid()).child("standBy").setValue(true);
                            databaseUsers.child(user.getUid()).child("programs").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data1 : dataSnapshot.getChildren()) {
                                        for (DataSnapshot data2 : data1.getChildren()) {
                                            if (data2.getKey().equals("on")) {
                                                if ((boolean) data2.getValue())  {
                                                    databaseUsers.child(user.getUid()).child("programs").child(data1.getKey()).child("on").setValue(false);
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            timer.cancel();
                            finishAffinity();
                            startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }
}
