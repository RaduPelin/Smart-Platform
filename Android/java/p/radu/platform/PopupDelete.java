package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PopupDelete extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;

    //defining view objects
    private Button buttonYes;
    private Button buttonNo;
    private TextView textViewAssurance;

    //defining privates
    private FirebaseUser  user;
    private boolean standBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_assurance);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.65), (int) (height *.35));

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting current user authentications data
        user = firebaseAuth.getCurrentUser();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                standBy = dataSnapshot.getValue(User.class).isStandBy();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        textViewAssurance = findViewById(R.id.textViewAssurance);
        textViewAssurance.setText(getString(R.string.DeleteAccount));

        buttonNo = findViewById(R.id.buttonNo);
        buttonYes = findViewById(R.id.buttonOk);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), ActivityMyAccount.class));
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null) {
                    if (standBy) {
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    databaseUsers.child(user.getUid()).removeValue();
                                    displayMessage(getString(R.string.Delete), 2000);
                                    finishAffinity();
                                    startActivity(new Intent(getApplicationContext(), ActivityMain.class));
                                } else {
                                    displayMessage(getString(R.string.DeleteError), 3000);
                                    finishAffinity();
                                    startActivity(new Intent(getApplicationContext(), ActivityMyAccount.class));
                                }
                            }

                        });
                    } else {
                        displayMessage(getString(R.string.DeleteConnectedError), 2000);
                        finishAffinity();
                        startActivity(new Intent(getApplicationContext(), ActivityMyAccount.class));
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupDelete.this, message, Toast.LENGTH_LONG);
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
