package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

import java.util.List;

public class PopupAssuranceDeleteProgram extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;

    //defining view objects
    private Button buttonYes;
    private Button buttonNo;
    private List<Program> programs;
    private String programName;
    private TextView textViewAssurance;

    //defining privates
    private FirebaseUser user;

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

        final Intent takePrograms = getIntent();

        Bundle bundle = takePrograms.getBundleExtra("PROGRAMS_EXTRA");

        if (bundle != null) {
            programs = Parcels.unwrap(bundle.getParcelable("PROGRAMS_PARCEL"));
        }

        String program = takePrograms.getStringExtra("PROGRAM_NAME");

        if (program != null) {
            programName = program;
        }

        textViewAssurance = findViewById(R.id.textViewAssurance);
        textViewAssurance.setText(takePrograms.getStringExtra("MESSAGE"));

        buttonYes = findViewById(R.id.buttonOk);
        buttonNo = findViewById(R.id.buttonNo);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null) {
                    for (int i = 0; i < programs.size(); i++) {
                        if (programName.equals(programs.get(i).getName())) {
                            databaseUsers.child(user.getUid()).child("programs").child(programName).removeValue();
                            finishAffinity();
                            startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                        }
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });
    }
    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupAssuranceDeleteProgram.this, message, Toast.LENGTH_LONG);
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
