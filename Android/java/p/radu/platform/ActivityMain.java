package p.radu.platform;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;
import static java.lang.Thread.sleep;


public class ActivityMain extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;

    //defining view objects
    private Button buttonScan;
    private TextView textViewSignin;
    private TextView textViewSignup;

    //defining privates
    private ZXingScannerView scannerView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing firebase app object
        FirebaseApp.initializeApp(this);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //If there is a network connection
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.Wait));
        progressDialog.show();
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conManager.getActiveNetworkInfo() != null) {
            progressDialog.dismiss();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(getApplicationContext(), ActivityMain.class));
                }
            }, 10000);

        }

        //if getCurrentUser does not returns null and the account is valid
        if (firebaseAuth.getCurrentUser() != null) {
                progressDialog.setMessage("Configuring...");
                progressDialog.show();
                databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
                databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((boolean) dataSnapshot.child("valid").getValue()) {
                            //that means user is already logged in
                            //so close this activity
                            progressDialog.dismiss();
                            finish();
                            //and open profile activity
                            startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                        } else {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }

        buttonScan = (Button) findViewById(R.id.buttonScan);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin1);
        textViewSignup = (TextView) findViewById(R.id.textViewSignup1);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                generate();
            }
        });

        textViewSignin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
            }
        });

        textViewSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ActivityRegister.class));
            }
        });
    }

    public void generate() {
        scannerView = new ZXingScannerView(this);
        if (checkPermission()) {
            setContentView(scannerView);
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        } else {
            requestPermission();
        }

    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void handleResult(Result result) {
        String code;
        code = result.getText();
        finish();
        Intent registerWithID = new Intent(ActivityMain.this, ActivityRegister.class);
        registerWithID.putExtra(getResources().getStringArray(R.array.Extra)[0], code);
        startActivity(registerWithID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(requestCode == 0 && grantResults.length < 1)) {
            scannerView = new ZXingScannerView(this);
            if (checkPermission()) {
                setContentView(scannerView);
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
        }
    }
}
