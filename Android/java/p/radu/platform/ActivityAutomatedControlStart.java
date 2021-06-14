package p.radu.platform;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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

public class ActivityAutomatedControlStart extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Button buttonAccount;
    private Button buttonManualControl;
    private Button buttonNewProgram;
    private List<Program> programs;
    private Spinner spinnerProgram;
    private TextView textViewTitle;
    private LinearLayout layoutButton;

    //defining privates
    private String initials;
    private FirebaseUser user;
    private String deviceID;
    private boolean isOn;
    private String programOn;
    private boolean isInStandBy;
    private Timer timer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automated_controller_start);

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

        programs = new ArrayList<>();

        databaseUsers.child(user.getUid()).child("programs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataLevel1 : dataSnapshot.getChildren()) {
                    Program program = new Program();

                    program.setName(dataLevel1.getKey());

                    for (DataSnapshot dataLevel2 : dataLevel1.getChildren()) {
                        if (dataLevel2.getKey().equals("comeBack")) {
                            program.setComeBack((boolean) dataLevel2.getValue());
                        }

                        if (dataLevel2.getKey().equals("on")) {
                            program.setOn((boolean) dataLevel2.getValue());
                        }

                        if (dataLevel2.getKey().equals("steps")) {
                            List<Step> steps = new ArrayList<>();

                            for (DataSnapshot dataLevel3 : dataLevel2.getChildren()) {
                                if (dataLevel3.getKey().equals("total")) {
                                    continue;
                                }
                                Step step = dataLevel3.getValue(Step.class);
                                steps.add(step);

                            }
                            program.setSteps(steps);

                        }
                    }

                    programs.add(program);
                }
                showContent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            public void onDrawerSlide(View drawerView, float slideOffset) {
                buttonAccount = findViewById(R.id.buttonAccount);
                buttonAccount.setText(initials);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
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

        buttonManualControl = findViewById(R.id.buttonManualControlStart);

        buttonManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild("program")) {
                                timer.cancel();
                                finish();
                                startActivity(new Intent(ActivityAutomatedControlStart.this, ActivityManualController.class));
                            } else {
                                displayMessage(getString(R.string.ManualControllerError), 3000);
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
        });

        buttonNewProgram = findViewById(R.id.buttonNewProgram);

        buttonNewProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                finish();
                startActivity(new Intent(ActivityAutomatedControlStart.this, ActivityAutomatedControlCreate.class));
            }
        });

    }

    private void showSteps(LinearLayout layout, List<Step> steps) {
        TextView stepsText  = new TextView(this);
        stepsText.setText(getString(R.string.Steps));
        stepsText.setTextColor(getColor(R.color.white));
        stepsText.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        stepsText.setTextSize(24);
        stepsText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        layout.addView(stepsText);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) stepsText.getLayoutParams();
        layoutParams.setMargins(100, 0, 100, 0);
        stepsText.setLayoutParams(layoutParams);

        for (int i = 0; i< steps.size(); i++) {
            Step step = steps.get(i);
            String title = "Step " + (i + 1);
            String direction = "Direction: " + step.getDirection();
            String distance = "Distance: " + step.getDistance() + " " + step.getDistanceUnits();
            String numberOfLapses = "Number of lapses: " + step.getNumberOfLapses();
            String delay = "Delay Per Lapse: " + step.getDelay() + " " + step.getDelayUnits();

            TextView titleView = new TextView(this);
            titleView.setText(title);
            titleView.setBackgroundColor(getColor(R.color.viewfinder_mask));
            titleView.setHeight(85);
            titleView.setTextColor(getColor(R.color.white));
            titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            titleView.setTextSize(18);
            titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            layout.addView(titleView);
            layoutParams = (LinearLayout.LayoutParams) titleView.getLayoutParams();
            layoutParams.setMargins(150, 60, 150, 0);
            titleView.setLayoutParams(layoutParams);

            TextView directionView = new TextView(this);
            directionView.setText(direction);
            directionView.setBackgroundColor(getColor(R.color.viewfinder_mask));
            directionView.setHeight(85);
            directionView.setTextColor(getColor(R.color.white));
            directionView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            directionView.setTextSize(12);
            directionView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            directionView.setPadding(50, 0, 50, 0);

            layout.addView(directionView);
            layoutParams = (LinearLayout.LayoutParams) directionView.getLayoutParams();
            layoutParams.setMargins(150, 0, 150, 0);
            directionView.setLayoutParams(layoutParams);

            TextView distanceView = new TextView(this);
            distanceView.setText(distance);
            distanceView.setBackgroundColor(getColor(R.color.viewfinder_mask));
            distanceView.setHeight(85);
            distanceView.setTextColor(getColor(R.color.white));
            distanceView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            distanceView.setTextSize(12);
            distanceView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            distanceView.setPadding(50, 0, 50, 0);

            layout.addView(distanceView);
            layoutParams = (LinearLayout.LayoutParams) distanceView.getLayoutParams();
            layoutParams.setMargins(150, 0, 150, 0);
            distanceView.setLayoutParams(layoutParams);

            TextView numberOfLapsesView = new TextView(this);
            numberOfLapsesView.setText(numberOfLapses);
            numberOfLapsesView.setBackgroundColor(getColor(R.color.viewfinder_mask));
            numberOfLapsesView.setHeight(85);
            numberOfLapsesView.setTextColor(getColor(R.color.white));
            numberOfLapsesView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            numberOfLapsesView.setTextSize(12);
            numberOfLapsesView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            numberOfLapsesView.setPadding(50, 0, 50, 0);

            layout.addView(numberOfLapsesView);
            layoutParams = (LinearLayout.LayoutParams) distanceView.getLayoutParams();
            layoutParams.setMargins(150, 0, 150, 0);
            numberOfLapsesView.setLayoutParams(layoutParams);

            TextView delayView = new TextView(this);
            delayView.setText(delay);
            delayView.setBackgroundColor(getColor(R.color.viewfinder_mask));
            delayView.setHeight(85);
            delayView.setTextColor(getColor(R.color.white));
            delayView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            delayView.setTextSize(12);
            delayView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            delayView.setPadding(50, 0, 50, 0);

            layout.addView(delayView);
            layoutParams = (LinearLayout.LayoutParams) distanceView.getLayoutParams();
            layoutParams.setMargins(150, 0, 150, 0);
            delayView.setLayoutParams(layoutParams);
        }
    }

    @SuppressWarnings("deprecation")
    private void showComponents(LinearLayout layout, final String programName, final boolean comeBack, boolean on, final List<Step> steps) {

        final boolean platformComeBack = comeBack;
        final boolean programIsOn = on;

        LinearLayout componentsLayout = new LinearLayout(this);
        componentsLayout.setOrientation(LinearLayout.HORIZONTAL);

        final ImageButton come = new ImageButton(this);

        if (platformComeBack) {
            come.setBackgroundResource(R.drawable.radio_button_pressed);
        } else {
            come.setBackgroundResource(R.drawable.radio_button_unpressed);
        }

        componentsLayout.addView(come);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) come.getLayoutParams();
        layoutParams.height = 100;
        layoutParams.width = 100;
        layoutParams.setMargins(200, 70 , 0, 20);
        come.setLayoutParams(layoutParams);

        come.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    if (!programIsOn) {
                        if (platformComeBack) {
                            databaseUsers.child(user.getUid()).child("programs").child(programName).child("comeBack").setValue(false);
                            come.setBackgroundResource(R.drawable.radio_button_unpressed);
                            finish();
                            startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                        } else {
                            databaseUsers.child(user.getUid()).child("programs").child(programName).child("comeBack").setValue(true);
                            finish();
                            startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                        }
                    } else {
                        displayMessage(getString(R.string.ChangeProgramError), 3000);
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }

            }
        });

        TextView comeBackView = new TextView(this);
        comeBackView.setText(getString(R.string.ComeBack));
        comeBackView.setTextColor(getColor(R.color.white));
        comeBackView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        comeBackView.setTextSize(18);
        comeBackView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        componentsLayout.addView(comeBackView);
        layoutParams = (LinearLayout.LayoutParams) comeBackView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(10, 70 , 0, 20);

        final ImageButton remove = new ImageButton(this);
        remove.setBackgroundResource(R.drawable.cancel);
        remove.setContentDescription(programName);

        componentsLayout.addView(remove);
        layoutParams = (LinearLayout.LayoutParams) remove.getLayoutParams();
        layoutParams.height = 130;
        layoutParams.width = 130;
        layoutParams.setMargins(120, 50 , 0, 20);
        remove.setLayoutParams(layoutParams);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    if (!programIsOn) {
                        for (int j = 0; j < programs.size(); j++) {
                            if (remove.getContentDescription().equals(programName)) {
                                Intent assurance = new Intent(ActivityAutomatedControlStart.this, PopupAssuranceDeleteProgram.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("PROGRAMS_PARCEL", Parcels.wrap(programs));
                                assurance.putExtra("PROGRAMS_EXTRA", bundle);
                                assurance.putExtra("PROGRAM_NAME", programName);
                                assurance.putExtra("MESSAGE", getResources().getStringArray(R.array.RemoveAssurance)[1] + " " + programName + " ?");
                                startActivity(assurance);
                            }
                        }
                    } else {
                        displayMessage(getString(R.string.ChangeProgramError), 3000);
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        layout.addView(componentsLayout);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        final Button buttonStartStop = new Button(this);
        buttonStartStop.setBackgroundTintList(getResources().getColorStateList(R.color.button_background_tint));
        if (programIsOn) {
            buttonStartStop.setText(getString(R.string.Stop));
        } else {
            buttonStartStop.setText(getString(R.string.Start));
        }

        buttonLayout.addView(buttonStartStop);
        layoutParams = (LinearLayout.LayoutParams) buttonStartStop.getLayoutParams();
        layoutParams.height = 120;
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.setMargins(200, 50 , 200, 0);

        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                    if (!isInStandBy) {
                        if (isOn && !programIsOn) {
                            displayMessage(getString(R.string.StartProgramError) + " " + programOn, 3000);
                        } else {
                            if (programIsOn) {
                                databaseDevices.child(deviceID).child("program").removeValue();
                                databaseUsers.child(user.getUid()).child("programs").child(programName).child("on").setValue(false);
                                displayMessage("Program " + programName + " is off", 2000);
                                timer.cancel();
                                finish();
                                startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                            } else {
                                databaseUsers.child(user.getUid()).child("programs").child(programName).child("on").setValue(true);
                                databaseDevices.child(deviceID).child("program").child("name").setValue(programName);
                                databaseDevices.child(deviceID).child("program").child("comeBack").setValue(platformComeBack);
                                databaseDevices.child(deviceID).child("program").child("steps").child("total").setValue(steps.size());

                                for (int i = 0; i < steps.size(); i++) {
                                    databaseDevices.child(deviceID).child("program").child("steps").child("Step " + (i + 1)).setValue(steps.get(i));
                                }
                                displayMessage("Program " + programName + " is on", 2000);
                                timer.cancel();
                                finish();
                                startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                            }
                        }
                    } else {
                        displayMessage(getString(R.string.Connect), 4000);
                    }
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        layout.addView(buttonLayout);
    }

    private void showContent() {
        if (programs.size() == 0) {
            LinearLayout layout = findViewById(R.id.scrollViewProgramLayout);
            layout.removeAllViews();
            ScrollView viewProgram = findViewById(R.id.scrollViewProgram);
            viewProgram.removeAllViews();
            viewProgram.addView(layout);
            LinearLayout alert  = new LinearLayout(this);
            alert.setOrientation(LinearLayout.HORIZONTAL);

            layout.addView(alert);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) alert.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            alert.setLayoutParams(layoutParams);

            ImageView alertImage  = new ImageView(this);
            alertImage.setBackgroundResource(R.drawable.alert);

            alert.addView(alertImage);
            layoutParams = (LinearLayout.LayoutParams) alertImage.getLayoutParams();
            layoutParams.width = 200;
            layoutParams.height = 200;
            layoutParams.setMargins(140, 0, 0, 0);
            alertImage.setLayoutParams(layoutParams);

            TextView alertText  = new TextView(this);
            alertText.setText(getString(R.string.NoProgramWorning));
            alertText.setTextColor(getColor(R.color.white));
            alertText.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
            alertText.setTextSize(24);
            alertText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            alert.addView(alertText);
            layoutParams = (LinearLayout.LayoutParams) alertText.getLayoutParams();
            layoutParams.width = 600;
            layoutParams.height = 200;

            alertText.setLayoutParams(layoutParams);

            LinearLayout solution  = new LinearLayout(this);
            solution.setOrientation(LinearLayout.HORIZONTAL);

            layout.addView(solution);
            layoutParams = (LinearLayout.LayoutParams) solution.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            solution.setLayoutParams(layoutParams);

            TextView solutionText  = new TextView(this);
            solutionText.setText(getString(R.string.AddFirstProgram));
            solutionText.setTextColor(getColor(R.color.white));
            solutionText.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
            solutionText.setTextSize(24);
            solutionText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            solution.addView(solutionText);
            layoutParams = (LinearLayout.LayoutParams) solutionText.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.setMargins(200, 80, 200, 0);
            solutionText.setLayoutParams(layoutParams);

            LinearLayout solver = new LinearLayout(this);
            solver.setOrientation(LinearLayout.HORIZONTAL);

            layout.addView(solver);
            layoutParams = (LinearLayout.LayoutParams) solver.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = 400;
            solver.setLayoutParams(layoutParams);

            ImageButton solverButton = new ImageButton(this);
            solverButton.setBackgroundResource(R.drawable.add);

            solver.addView(solverButton);
            layoutParams = (LinearLayout.LayoutParams) solverButton.getLayoutParams();
            layoutParams.width = 320;
            layoutParams.height = 320;
            layoutParams.setMargins(370, 50, 0, 0);
            solverButton.setLayoutParams(layoutParams);

            solverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlCreate.class));
                }
            });

            layoutButton = findViewById(R.id.layoutDinamic);
            layoutParams = (LinearLayout.LayoutParams) layoutButton.getLayoutParams();
            layoutParams.setMargins(0, 185, 0, 0);
            layoutButton.setLayoutParams(layoutParams);
        } else {
            spinnerProgram = findViewById(R.id.spinnerProgram);
            spinnerProgram.setVisibility(View.VISIBLE);


            List<String> allPrograms = new ArrayList<>();
            for (int i = 0; i < programs.size(); i++) {
                allPrograms.add(programs.get(i).getName());
                if (programs.get(i).isOn()) {
                    isOn = true;
                    programOn = programs.get(i).getName();
                }
            }
            ArrayAdapter<String> adapterProgram = new ArrayAdapter<String>(this,R.layout.spinner_thin, allPrograms);
            adapterProgram.setDropDownViewResource(R.layout.spinner_dropdown_thin);
            spinnerProgram.setAdapter(adapterProgram);

            spinnerProgram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    for (int i = 0; i < programs.size(); i++) {
                        if (programs.get(i).getName().equals(spinnerProgram.getSelectedItem())) {
                            LinearLayout layout = findViewById(R.id.scrollViewProgramLayout);
                            layout.removeAllViews();
                            ScrollView viewSteps = findViewById(R.id.scrollViewProgram);
                            viewSteps.removeAllViews();
                            viewSteps.addView(layout);
                            showSteps(layout, programs.get(i).getSteps());
                            showComponents(layout, programs.get(i).getName(), programs.get(i).isComeBack(), programs.get(i).isOn(), programs.get(i).getSteps());
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

            textViewTitle = findViewById(R.id.textViewDinamicTitle);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textViewTitle.getLayoutParams();
            layoutParams.height = 100;
            textViewTitle.setLayoutParams(layoutParams);

            layoutButton = findViewById(R.id.layoutDinamic);
            layoutParams = (LinearLayout.LayoutParams) layoutButton.getLayoutParams();
            layoutParams.setMargins(0, 100, 0, 0);
            layoutButton.setLayoutParams(layoutParams);

        }
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityAutomatedControlStart.this, message, Toast.LENGTH_LONG);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
