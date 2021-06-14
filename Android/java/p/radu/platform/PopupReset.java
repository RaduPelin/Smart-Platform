package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PopupReset extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining view objects
    private Button buttonYes;
    private Button buttonNo;
    private EditText editTextEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_reset);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.75), (int) (height *.25));

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmailReset);
        buttonYes = findViewById(R.id.buttonReset);
        buttonNo = findViewById(R.id.buttonCancelReset);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                finishAffinity();
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null) {
                    String email = editTextEmail.getText().toString();
                    if (TextUtils.isEmpty(email)) {
                        displayMessage(getString(R.string.ResetEmailError), 2000);
                    } else {
                        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    displayMessage(getString(R.string.Reset), 2000);
                                    finishAffinity();
                                    startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                                } else {
                                    displayMessage(getString(R.string.ResetError), 2000);
                                }
                            }
                        });
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }

            }
        });


    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupReset.this, message, Toast.LENGTH_LONG);
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
