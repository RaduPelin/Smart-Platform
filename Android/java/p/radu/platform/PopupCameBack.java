package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PopupCameBack extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private Button buttonYes;
    private Button buttonNo;
    private TextView textViewAssurance;

    //defining privates
    private FirebaseUser user;
    private String deviceID;
    private String programName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_assurance);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.65), (int) (height *.35));

        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alert);
        mp.start();

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting current user authentications data
        user = firebaseAuth.getCurrentUser();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));

        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        buttonYes = findViewById(R.id.buttonOk);
        buttonNo = findViewById(R.id.buttonNo);

        buttonNo.setText(R.string.CloseBtn);
        buttonYes.setText("");

        buttonYes.setVisibility(Button.INVISIBLE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) buttonNo.getLayoutParams();
        layoutParams.setMargins(450, 0, 0, 0);
        buttonNo.setLayoutParams(layoutParams);

        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deviceID = dataSnapshot.getValue(User.class).getDeviceID();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        textViewAssurance = findViewById(R.id.textViewAssurance);
        textViewAssurance.setText(getString(R.string.CameBack));

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("cameBackFrom").exists()) {
                                databaseUsers.child(user.getUid()).child("programs").child(dataSnapshot.child("cameBackFrom").getValue().toString()).child("on").setValue(false);
                                databaseDevices.child(deviceID).child("cameBackFrom").removeValue();
                                finishAffinity();
                                startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                    return;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
            databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("cameBackFrom").exists()) {
                        databaseUsers.child(user.getUid()).child("programs").child(dataSnapshot.child("cameBackFrom").getValue().toString()).child("on").setValue(false);
                        databaseDevices.child(deviceID).child("cameBackFrom").removeValue();
                        finishAffinity();
                        startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
            databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("cameBackFrom").exists()) {
                        databaseUsers.child(user.getUid()).child("programs").child(dataSnapshot.child("cameBackFrom").getValue().toString()).child("on").setValue(false);
                        databaseDevices.child(deviceID).child("cameBackFrom").removeValue();
                        finishAffinity();
                        startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        super.onPause();
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupCameBack.this, message, Toast.LENGTH_LONG);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

}

