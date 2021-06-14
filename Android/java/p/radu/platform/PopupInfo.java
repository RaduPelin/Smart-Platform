package p.radu.platform;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PopupInfo extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private TextView ssid;
    private TextView password;

    //defining privates
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_info);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.8), (int) (height *.7));

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting current user authentications data
        user = firebaseAuth.getCurrentUser();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        ssid = findViewById(R.id.textViewSsid);
        password = findViewById(R.id.textViewPassword);

        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseDevices.child(dataSnapshot.getValue(User.class).getDeviceID()).child("default").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ssid.setText("SSID: " + dataSnapshot.child("ssid").getValue());
                        password.setText("PASSWORD: " + dataSnapshot.child("password").getValue());
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
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();


    }

    @Override
    public void onPause() {
        finish();
        super.onPause();
    }
}
