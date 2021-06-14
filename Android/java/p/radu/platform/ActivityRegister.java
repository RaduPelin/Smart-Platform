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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static p.radu.platform.R.layout.activity_register;

public class ActivityRegister extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private  DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private EditText editTextfirstName;
    private EditText editTextlastName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextReentered;
    private EditText editTextPhone;
    private EditText editTextDeviceID;
    private Button buttonSignup;
    private ProgressDialog progressDialog;
    private TextView textViewSignin;

    //defining privates
    private boolean noDeviceID;
    List<String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_register);

        //initializing firebase app object
        FirebaseApp.initializeApp(this);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //getting user database data
        databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        //initializing views
        editTextfirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextlastName = (EditText) findViewById(R.id.editTextLastName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextReentered = (EditText) findViewById(R.id.editTextReentered);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        editTextDeviceID = (EditText) findViewById(R.id.editTextDeviceID);
        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(this);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin2);
        noDeviceID = true;

        devices = new ArrayList<String>();

        databaseDevices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    devices.add(data.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //attaching listener to button
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                registerUser();
            }
        });

        textViewSignin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
            }
        });

        String deviceID;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            deviceID = bundle.getString(getResources().getStringArray(R.array.Extra)[0]);
            editTextDeviceID.setText(deviceID);
            editTextDeviceID.setFocusable(false);
        }
    }



    private void registerUser() {
        //getting email and password from edit texts
        String fName = editTextfirstName.getText().toString();
        String lName = editTextlastName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password  = editTextPassword.getText().toString();
        String reentered  = editTextReentered.getText().toString();
        String phone = editTextPhone.getText().toString();
        final String deviceID = editTextDeviceID.getText().toString();

        //checking if email and passwords are empty or invalid
        if (TextUtils.isEmpty(fName)) {
            displayMessage(getString(R.string.FirstError), 2000);
            return;
        }

        if (TextUtils.isEmpty(lName)) {
            displayMessage(getString(R.string.LastError), 2000);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            displayMessage(getString(R.string.EmailError), 2000);
            return;
        } else {
            String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            if (!email.matches(regex)) {
                displayMessage(getString(R.string.EmailInvalidError), 2000);
                return;
            }
        }

        if (TextUtils.isEmpty(password)) {
            displayMessage(getString(R.string.PasswordError), 2000);
            return;
        } else {
            String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
            if (!password.matches(regex)) {
                displayMessage(getString(R.string.PasswordInvalidError), 5000);
                return;
            }
        }

        if (TextUtils.isEmpty(reentered)) {
            displayMessage(getString(R.string.RePasswordError), 2000);
            return;
        }

        if (!password.equals(reentered)) {
            displayMessage(getString(R.string.NotTheSameError), 2000);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            displayMessage(getString(R.string.PhoneError), 2000);
            return;
        } else {
            String regex = "\\+\\d(-\\d{3}){2}-\\d{4}";
            if (!phone.matches(regex)) {
                displayMessage(getString(R.string.PhoneInvalidError), 2000);
                return;
            }
        }

        if (TextUtils.isEmpty(deviceID)) {
            displayMessage(getString(R.string.DeviceIDError), 2000);
            return;
        }



        for (String device : devices) {
            if (device.equals(deviceID)) {
                noDeviceID = false;
            }
        }

        if (noDeviceID) {
            noDeviceID = true;
            displayMessage(getString(R.string.NoDeviceError), 2500);
            return;
        }

            //if the email and password are not empty
            //displaying a progress dialog
            progressDialog.setMessage(getString(R.string.Registering));
            progressDialog.show();

            //creating a new user
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checking if success
                            if (task.isSuccessful()) {
                                firebaseAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    displayMessage(getString(R.string.ValidateEmail), 2000);
                                                    finish();
                                                    startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                                                } else {
                                                    displayMessage(getString(R.string.ValidateEmailError), 2000);
                                                }
                                            }
                                        });
                                displayMessage(getString(R.string.RegisterSuccess), 2000);
                            } else {
                                //display some message here
                                displayMessage(getString(R.string.RegisterError), 2000);
                            }

                            progressDialog.dismiss();

                            //if the task is successfull
                            if (task.isSuccessful()) {
                                String id = firebaseAuth.getUid();
                                String fullName = editTextfirstName.getText().toString() + " " + editTextlastName.getText().toString();
                                String email = editTextEmail.getText().toString();
                                String phone = editTextPhone.getText().toString();
                                String deviceID = editTextDeviceID.getText().toString();
                                //creating an Artist Object
                                User newUser = new User(fullName, email, phone, deviceID, false, false);
                                //Saving the Artist
                                databaseUsers.child(id).setValue(newUser);

                                databaseDevices.child(deviceID).child("default").child("ssid").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        databaseUsers.child(firebaseAuth.getUid()).child("ssid").setValue(""+dataSnapshot.getValue());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                databaseDevices.child(deviceID).child("default").child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        databaseUsers.child(firebaseAuth.getUid()).child("password").setValue(""+dataSnapshot.getValue());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



                            }
                        }
                    });
        }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityRegister.this, message, Toast.LENGTH_LONG);
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

