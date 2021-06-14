package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class ActivityMyAccount extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private TextView fullName;
    private TextView email;
    private TextView deviceID;
    private TextView phoneNumber;
    private TextView dezactivate;
    private Button buttonGoToController;

    //defining privates
    private FirebaseUser user;
    private Timer timer;
    private boolean canDelete;
    private boolean isInStandBy;
    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

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

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting current user authentications data
        user = firebaseAuth.getCurrentUser();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));

        //getting user database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        fullName = findViewById(R.id.textViewFullName);
        email = findViewById(R.id.textViewEmail);
        deviceID = findViewById(R.id.textViewDeviceID);
        phoneNumber = findViewById(R.id.textViewPhoneNumber);


        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                fullName.setText("Full Name: " + user.getFullName());
                email.setText("Email: " + user.getEmail());
                deviceID.setText("Device ID: " + user.getDeviceID());
                device = user.getDeviceID();
                phoneNumber.setText("Phone Number: " + user.getPhone());
                isInStandBy = (boolean) dataSnapshot.child("standBy").getValue();

                databaseDevices.child(user.getDeviceID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild("program")) {
                            canDelete = true;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dezactivate = findViewById(R.id.textViewDezactivate);

        dezactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    if (canDelete) {
                        startActivity(new Intent(getApplicationContext(), PopupDelete.class));
                    } else {
                        displayMessage(getString(R.string.DeleteAccountError), 3000);
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });


        buttonGoToController = findViewById(R.id.buttonGoToController);

        buttonGoToController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    timer.cancel();
                    finish();
                    startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityMyAccount.this, message, Toast.LENGTH_LONG);
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
            if (device != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(device).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("program").exists() && !isInStandBy){
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
            if (device != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(device).addListenerForSingleValueEvent(new ValueEventListener() {
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
            if (device != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(device).addListenerForSingleValueEvent(new ValueEventListener() {
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
            if (device != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(device).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("connected").exists()){
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
