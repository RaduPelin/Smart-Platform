package p.radu.platform;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ActivityLogin extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;

    //defining views objects
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;
    private ProgressDialog progressDialog;
    private TextView textViewResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignin);
        textViewSignup = (TextView) findViewById(R.id.textViewSignup);
        progressDialog = new ProgressDialog(this);

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));

        //attaching listener to button
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                userLogin();
            }
        });

        textViewSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), ActivityMain.class));
            }
        });

        textViewResetPassword = findViewById(R.id.textViewResetPassword);
        textViewResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(getApplicationContext(), PopupReset.class));
            }
        });
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString();
        String password  = editTextPassword.getText().toString();


        //checking if email and password are empty or invalid
        if (TextUtils.isEmpty(email)) {
            displayMessage(getString(R.string.EmailError), 2000);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            displayMessage(getString(R.string.PasswordError), 2000);
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog
        progressDialog.setMessage(getString(R.string.SignIn));
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    //if the task is successfull
                    if (task.isSuccessful()) {
                        if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                            databaseUsers.child(firebaseAuth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (!user.isValid()) {
                                            databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).child("valid").setValue(true);
                                            displayMessage(getString(R.string.AccountActivated), 2000);
                                        }
                                        databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).child("standBy").setValue(true);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                            });

                            //start the profile activity
                            finish();
                            Intent intent = new Intent(ActivityLogin.this, ActivityAutomatedControlStart.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            displayMessage(getString(R.string.ValidateEmail),3000);
                        }

                    } else {
                        displayMessage(getString(R.string.CredentialsError), 2000);
                    }
                }
            });

    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityLogin.this, message, Toast.LENGTH_LONG);
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

