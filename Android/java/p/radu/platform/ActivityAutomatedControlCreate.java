package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityAutomatedControlCreate extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private Spinner spinnerDirection;
    private Spinner spinnerDistance;
    private Spinner spinnerDelay;
    private EditText editTextProgramName;
    private EditText editTextDistance;
    private EditText editTextNumberOfLapses;
    private EditText editTextDelay;
    private ImageButton cancel;
    private ImageButton add;
    private Button buttonSave;
    private Button buttonSteps;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Button buttonAccount;
    private Button buttonManualControl;
    private Button buttonMyPrograms;

    //defining privates
    private String initials;
    private List<Step> newSteps;
    private FirebaseUser user;
    private String deviceID;
    private boolean isInStandBy;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automated_controller_create);

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
        databaseUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.getValue(User.class).getFullName();
                String[] names = fullName.split(" ");
                initials = names[0].substring(0, 1) + " " + names[1].substring(0, 1);
                deviceID = dataSnapshot.getValue(User.class).getDeviceID();
                isInStandBy = dataSnapshot.getValue(User.class).isStandBy();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //getting device database data
        databaseDevices = FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB));

        drawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.Open, R.string.Close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigation = findViewById(R.id.navigation);
        navigation.bringToFront();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                buttonAccount = findViewById(R.id.buttonAccount);
                buttonAccount.setText(initials);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                buttonAccount = findViewById(R.id.buttonAccount);
                buttonAccount.setText(initials);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                drawer.closeDrawers();

                if (id == R.id.account) {
                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                        timer.cancel();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ActivityMyAccount.class));
                    } else {
                        displayMessage(getString(R.string.Wait), 3000);
                    }
                } else if (id == R.id.settings) {
                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                        timer.cancel();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
                    } else {
                        displayMessage(getString(R.string.Wait), 3000);
                    }
                } else if (id == R.id.logout) {
                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                        databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild("program")) {
                                    if (!isInStandBy) {
                                        databaseDevices.child(deviceID).child("connected").removeValue();
                                        databaseDevices.child(deviceID).child("otherNetwork").removeValue();
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
                                    databaseUsers.child(user.getUid()).child("standBy").setValue(false);
                                    timer.cancel();
                                    finish();
                                    firebaseAuth.signOut();
                                    startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                                    displayMessage(getString(R.string.Logout), 1500);
                                } else {
                                    displayMessage(getString(R.string.LogoutError), 3000);
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
                return true;
            }

        });

        buttonManualControl = findViewById(R.id.buttonManualControlCreate);

        buttonManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    timer.cancel();
                    finish();
                    startActivity(new Intent(ActivityAutomatedControlCreate.this, ActivityManualController.class));
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });


        buttonMyPrograms = findViewById(R.id.buttonMyPrograms);

        buttonMyPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    timer.cancel();
                    finish();
                    startActivity(new Intent(ActivityAutomatedControlCreate.this, ActivityAutomatedControlStart.class));

                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        Intent takeSteps = getIntent();
        Bundle bundle = takeSteps.getBundleExtra("STEPS_EXTRA");
        if (bundle != null) {
            newSteps = Parcels.unwrap(bundle.getParcelable("STEPS_PARCEL"));

        } else {
            newSteps = new ArrayList<>();

        }

        editTextProgramName = findViewById(R.id.editTextProgramName);
        String program = takeSteps.getStringExtra("PROGRAM_NAME");
        if (program != null) {
            editTextProgramName.setText(program);
        }

        editTextDistance = findViewById(R.id.editTextDistance);
        editTextNumberOfLapses = findViewById(R.id.editTextNumberOfLapses);
        editTextDelay = findViewById(R.id.editTextDelay);

        final ArrayAdapter<String> adapterDirection = new ArrayAdapter<String>(this,R.layout.spinner_big, getResources().getStringArray(R.array.Directions)) {
            @Override
            public int getCount() {
                return getResources().getStringArray(R.array.Directions).length - 1; // Truncate the list
            }
        };

        final ArrayAdapter<String> adapterDistance = new ArrayAdapter<String>(this,R.layout.spinner_big, getResources().getStringArray(R.array.Distances)) {
            @Override
            public int getCount() {
                return getResources().getStringArray(R.array.Distances).length - 1; // Truncate the list
            }
        };

        final ArrayAdapter<String> adapterDelay = new ArrayAdapter<String>(this,R.layout.spinner_big, getResources().getStringArray(R.array.Delays)) {
            @Override
            public int getCount() {
                return getResources().getStringArray(R.array.Delays).length - 1; // Truncate the list
            }
        };

        adapterDirection.setDropDownViewResource(R.layout.spinner_dropdown_big);
        adapterDistance.setDropDownViewResource(R.layout.spinner_dropdown_big);
        adapterDelay.setDropDownViewResource(R.layout.spinner_dropdown_big);
        spinnerDirection = findViewById(R.id.spinnerDirection);
        spinnerDirection.setAdapter(adapterDirection);
        spinnerDirection.setSelection(adapterDirection.getCount());
        spinnerDistance = findViewById(R.id.spinnerDistance);
        spinnerDistance.setAdapter(adapterDistance);
        spinnerDistance.setSelection(adapterDistance.getCount());
        spinnerDelay = findViewById(R.id.spinnerDelay);
        spinnerDelay.setAdapter(adapterDelay);
        spinnerDelay.setSelection(adapterDelay.getCount());

        cancel = findViewById(R.id.clear);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerDirection.setAdapter(adapterDirection);
                spinnerDirection.setSelection(adapterDirection.getCount());
                spinnerDistance.setAdapter(adapterDistance);
                spinnerDistance.setSelection(adapterDistance.getCount());
                spinnerDelay.setAdapter(adapterDelay);
                spinnerDelay.setSelection(adapterDelay.getCount());
                editTextDistance.setText("");
                editTextNumberOfLapses.setText("");
                editTextDelay.setText("");

            }
        });

        add = findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String distance = editTextDistance.getText().toString();
                String lapses = editTextNumberOfLapses.getText().toString();
                String delay = editTextDelay.getText().toString();
                String direction;
                double distanceValue;
                String distanceUnits;
                int numberOfLapses;
                double delayValue;
                String delayUnits;

                if (spinnerDirection.getSelectedItem().toString().equals(getResources().getStringArray(R.array.Directions)[4])) {
                    displayMessage(getString(R.string.ChooseDirectionError), 2000);
                    return;
                } else {
                    direction = spinnerDirection.getSelectedItem().toString();

                }

                if (spinnerDistance.getSelectedItem().toString().equals(getResources().getStringArray(R.array.Distances)[2])) {
                    displayMessage(getString(R.string.ChooseDistanceUnitsError), 2000);
                    return;
                } else {
                    distanceUnits = spinnerDistance.getSelectedItem().toString();
                }

                if (TextUtils.isEmpty(distance)) {
                    displayMessage(getString(R.string.DistanceError), 2000);
                    return;
                } else {
                    if (!distance.matches("^\\d*\\.?\\d*$")) {
                        displayMessage(getString(R.string.InvalidDistanceError), 1500);
                        return;
                    } else {
                        if (Double.parseDouble(distance) > 999.99 && distanceUnits.equals(getResources().getStringArray(R.array.Distances)[0])) {
                            displayMessage(getString(R.string.InvalidCentimentersNumberError), 2000);
                            return;
                        } else {
                            if (Double.parseDouble(distance) > 2000.0 && distanceUnits.equals(getResources().getStringArray(R.array.Distances)[1])) {
                                displayMessage(getString(R.string.InvalidMetersNumberError), 2000);
                                return;
                            } else {
                                distanceValue = Double.parseDouble(distance);
                            }
                        }
                    }
                }

                if (TextUtils.isEmpty(lapses)) {
                    displayMessage(getString(R.string.LapseError), 1500);
                    return;
                } else {
                    if (!lapses.matches("^\\d+$")) {
                        displayMessage(getString(R.string.InvalideLapseError), 2000);
                        return;
                    } else {
                        numberOfLapses = Integer.parseInt(lapses);
                        if(numberOfLapses < 1) {
                            displayMessage(getString(R.string.AtLeastOneError), 2000);
                            return;
                        }
                    }
                }

                if (spinnerDelay.getSelectedItem().toString().equals(getResources().getStringArray(R.array.Delays)[3])) {
                    displayMessage(getString(R.string.ChooseDelayUnitsError), 2000);
                    return;
                } else {
                    delayUnits = spinnerDelay.getSelectedItem().toString();
                }

                if (TextUtils.isEmpty(delay)) {
                    displayMessage(getString(R.string.DelayError), 1500);
                    return;
                } else {
                    if (!delay.matches("^\\d*\\.?\\d*$")) {
                        displayMessage(getString(R.string.InvalidDelayError), 2000);
                        return;
                    } else {
                        if (Double.parseDouble(delay) > 59.0 && delayUnits.equals(getResources().getStringArray(R.array.Delays)[0])) {
                            displayMessage(getString(R.string.InvalidSecondsNumberError), 2000);
                            return;
                        } else {
                            if (Double.parseDouble(delay) > 59.99 && delayUnits.equals(getResources().getStringArray(R.array.Delays)[1])) {
                                displayMessage(getString(R.string.InvalidMinutesNumberError), 2000);
                                return;
                            } else {
                                if (Double.parseDouble(delay) > 48.0 && delayUnits.equals(getResources().getStringArray(R.array.Delays)[2])) {
                                    displayMessage(getString(R.string.InvalidHoursNumberError), 2000);
                                    return;
                                } else {
                                    delayValue = Double.parseDouble(delay);
                                }
                            }
                        }
                    }
                }

                newSteps.add(new Step(direction, distanceValue, distanceUnits, numberOfLapses, delayValue, delayUnits));

                spinnerDirection.setAdapter(adapterDirection);
                spinnerDirection.setSelection(adapterDirection.getCount());
                spinnerDistance.setAdapter(adapterDistance);
                spinnerDistance.setSelection(adapterDistance.getCount());
                spinnerDelay.setAdapter(adapterDelay);
                spinnerDelay.setSelection(adapterDelay.getCount());
                editTextDistance.setText("");
                editTextNumberOfLapses.setText("");
                editTextDelay.setText("");

                displayMessage(getResources().getStringArray(R.array.SuccessfullyAdded)[0], 2000);
            }


        });

        buttonSteps = findViewById(R.id.buttonSteps);

        buttonSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (newSteps.isEmpty()) {
                    displayMessage(getString(R.string.NoStepsError), 1500);
                } else {
                    Intent viewSteps = new Intent(ActivityAutomatedControlCreate.this, PopupSteps.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
                    viewSteps.putExtra("STEPS_EXTRA", bundle);
                    viewSteps.putExtra("PROGRAM_NAME", editTextProgramName.getText().toString());
                    viewSteps.putExtra("CAN_CLOSE", "Yes");
                    startActivity(viewSteps);
                }
            }
        });

        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null) {
                    String programName = editTextProgramName.getText().toString();

                    if (TextUtils.isEmpty(programName) || programName.equals(getString(R.string.Program))) {
                        displayMessage(getString(R.string.ProgramError), 2000);
                        return;
                    }

                    if (newSteps.isEmpty()) {
                        displayMessage(getString(R.string.StepsError), 2000);
                        return;
                    }

                    databaseUsers = FirebaseDatabase.getInstance().getReference(getString(R.string.UserDB));
                    databaseUsers.child(user.getUid()).child("programs").child(programName).child("steps").child("total").setValue(newSteps.size());
                    for (int i = 0; i < newSteps.size(); i++) {
                        databaseUsers.child(user.getUid()).child("programs").child(programName).child("steps").child("Step " + (i + 1)).setValue(newSteps.get(i));
                    }
                    databaseUsers.child(user.getUid()).child("programs").child(programName).child("comeBack").setValue(false);
                    databaseUsers.child(user.getUid()).child("programs").child(programName).child("on").setValue(false);

                    spinnerDirection.setAdapter(adapterDirection);
                    spinnerDirection.setSelection(adapterDirection.getCount());
                    spinnerDistance.setAdapter(adapterDistance);
                    spinnerDistance.setSelection(adapterDistance.getCount());
                    spinnerDelay.setAdapter(adapterDelay);
                    spinnerDelay.setSelection(adapterDelay.getCount());
                    editTextDistance.setText("");
                    editTextNumberOfLapses.setText("");
                    editTextDelay.setText("");
                    editTextProgramName.setText("");

                    displayMessage(getResources().getStringArray(R.array.SuccessfullyAdded)[1], 1500);
                    newSteps.clear();
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityAutomatedControlCreate.this, message, Toast.LENGTH_LONG);
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
                            if(dataSnapshot.child("program").hasChild("wait") && dataSnapshot.child("program").hasChild("alertOn")) {
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
                                    for(DataSnapshot data1 : dataSnapshot.getChildren()) {
                                        for(DataSnapshot data2 : data1.getChildren()) {
                                            if(data2.getKey().equals("on")) {
                                                if((boolean) data2.getValue())  {
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
