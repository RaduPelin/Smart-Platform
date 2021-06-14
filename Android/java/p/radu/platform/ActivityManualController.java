package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityManualController extends AppCompatActivity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //defining database object
    private DatabaseReference databaseUsers;
    private DatabaseReference databaseDevices;

    //defining view objects
    private Button buttonAccount;
    private Button buttonAutoControl;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private LinearLayout layout;
    private ImageButton front;
    private ImageButton back;
    private ImageButton left;
    private ImageButton right;
    private ImageButton start;
    private ImageButton accelerate;
    private ImageButton decelerate;

    //defining privates
    private String initials;
    private boolean[] isClicked = {false, false, false, false, false};
    private int speed;
    private FirebaseUser user;
    private String deviceID;
    private boolean isInStandBy;
    private Timer timer;
    private String blockedPosition = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_controller);

        //initializing view
        front = findViewById(R.id.front);
        back = findViewById(R.id.back);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        start = findViewById(R.id.start);
        accelerate = findViewById(R.id.accelerate);
        decelerate = findViewById(R.id.decelerate);
        buttonAutoControl = findViewById(R.id.buttonAutoControl);
        speed = 0;

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

        TimerTask timerAlert = new Alert();
        TimerTask timerAlertShow = new AlertShow();
        TimerTask timerLayoutShow = new LayoutShow();
        TimerTask timerBlockedShow = new BlockedShow();
        TimerTask timerButtonShow = new ButtonShow();
        TimerTask timerProgramFinished = new ProgramFinished();
        TimerTask timerCameBack = new CameBack();
        TimerTask timerNoInternet = new NoInternet();
        TimerTask timerConnected = new Connected();

        //running timer task as daemon thread
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerAlert, 0, 1000);
        timer.scheduleAtFixedRate(timerAlertShow, 0, 500);
        timer.scheduleAtFixedRate(timerLayoutShow, 0, 500);
        timer.scheduleAtFixedRate(timerBlockedShow, 0, 1000);
        timer.scheduleAtFixedRate(timerButtonShow , 0, 500);
        timer.scheduleAtFixedRate(timerProgramFinished, 0, 20000);
        timer.scheduleAtFixedRate(timerCameBack, 0, 20000);
        timer.scheduleAtFixedRate(timerNoInternet, 0, 12000);
        timer.scheduleAtFixedRate(timerConnected, 0, 1000);

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
                    if (new State(getString(R.string.PlatformStop)).modify()) {
                        timer.cancel();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ActivityMyAccount.class));
                    } else {
                        displayMessage(getString(R.string.Wait),3000);
                    }
                } else if (id == R.id.settings) {
                    if (new State(getString(R.string.PlatformStop)).modify()) {
                        timer.cancel();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
                    } else {
                        displayMessage(getString(R.string.Wait),3000);
                    }
                } else if (id == R.id.logout) {
                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                        databaseDevices.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild("program")) {
                                    if (new State(getString(R.string.PlatformStop)).modify()) {
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
                                            databaseUsers.child(user.getUid()).child("standBy").setValue(false);

                                        }
                                        timer.cancel();
                                        firebaseAuth.signOut();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                                        displayMessage(getString(R.string.Logout), 1500);
                                    } else {
                                        displayMessage(getString(R.string.Wait), 3000);
                                    }
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

        buttonAutoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new State(getString(R.string.PlatformStop)).modify()) {
                    timer.cancel();
                    finish();
                    startActivity(new Intent(getApplicationContext(), ActivityAutomatedControlStart.class));
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0]) {
                    if (isClicked[2] && !isClicked[3] && !isClicked[4]) {
                        if (new State(getResources().getStringArray(R.array.States)[3]).modify()) {
                            isClicked[2] = false;
                            back.setBackgroundResource(R.drawable.back);
                        } else {
                            displayMessage(getString(R.string.Wait), 3000);
                        }
                    } else {
                        if (!isClicked[3] && !isClicked[4]) {
                            if (new State(getResources().getStringArray(R.array.States)[2]).modify()) {
                                stopMove();
                                isClicked[2] = true;
                                back.setBackgroundResource(R.drawable.back_pressed);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        } else {
                            if (isClicked[2]) {
                                displayMessage(getString(R.string.DirectionError), 1500);
                            }
                        }

                        if (isClicked[1] && (isClicked[3] || isClicked[4])) {
                            if (isClicked[3]) {
                                if (new State(getResources().getStringArray(R.array.States)[6]).modify()) {
                                    isClicked[1] = false;
                                    front.setBackgroundResource(R.drawable.front);
                                    isClicked[2] = true;
                                    back.setBackgroundResource(R.drawable.back_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            } else {
                                if (new State(getResources().getStringArray(R.array.States)[10]).modify()) {
                                    isClicked[1] = false;
                                    front.setBackgroundResource(R.drawable.front);
                                    isClicked[2] = true;
                                    back.setBackgroundResource(R.drawable.back_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }

                        }
                    }
                } else {
                    displayMessage(getString(R.string.TurnOnError), 1000);
                }
            }
        });

        front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0]) {
                    if (isClicked[1] && !isClicked[3] && !isClicked[4]) {
                        if (new State(getResources().getStringArray(R.array.States)[1]).modify()) {
                            isClicked[1] = false;
                            front.setBackgroundResource(R.drawable.front);
                        } else {
                            displayMessage(getString(R.string.Wait), 3000);
                        }
                    } else {
                        if (!isClicked[3] && !isClicked[4]) {
                            if (new State(getResources().getStringArray(R.array.States)[0]).modify()) {
                                stopMove();
                                isClicked[1] = true;
                                front.setBackgroundResource(R.drawable.front_pressed);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        } else {
                            if (isClicked[1]) {
                                displayMessage(getString(R.string.DirectionError), 1500);
                            }
                        }

                        if (isClicked[2] && (isClicked[3] || isClicked[4])) {
                            if (isClicked[3]) {
                                if (new State(getResources().getStringArray(R.array.States)[4]).modify()) {
                                    isClicked[2] = false;
                                    back.setBackgroundResource(R.drawable.back);
                                    isClicked[1] = true;
                                    front.setBackgroundResource(R.drawable.front_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            } else {
                                if (new State(getResources().getStringArray(R.array.States)[8]).modify()) {
                                    isClicked[2] = false;
                                    back.setBackgroundResource(R.drawable.back);
                                    isClicked[1] = true;
                                    front.setBackgroundResource(R.drawable.front_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }

                        }
                    }
                } else {
                    displayMessage(getString(R.string.TurnOnError), 1000);
                }
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0] && (isClicked[1] || isClicked[2])) {
                    if (isClicked[3]) {
                        if (isClicked[1]) {
                            if (new State(getResources().getStringArray(R.array.States)[5]).modify()) {
                                isClicked[3] = false;
                                left.setBackgroundResource(R.drawable.left);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        } else {
                            if (new State(getResources().getStringArray(R.array.States)[7]).modify()) {
                                isClicked[3] = false;
                                left.setBackgroundResource(R.drawable.left);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        }
                    } else {
                        if (isClicked[1]) {
                            if (isClicked[4]) {
                                if (new State(getResources().getStringArray(R.array.States)[4]).modify()) {
                                    isClicked[4] = false;
                                    right.setBackgroundResource(R.drawable.right);
                                    isClicked[3] = true;
                                    left.setBackgroundResource(R.drawable.left_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            } else {
                                if (new State(getResources().getStringArray(R.array.States)[4]).modify()) {
                                    isClicked[3] = true;
                                    left.setBackgroundResource(R.drawable.left_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }
                        }

                        if (isClicked[2]) {
                            if (isClicked[4]) {
                                if (new State(getResources().getStringArray(R.array.States)[6]).modify()) {
                                    isClicked[4] = false;
                                    right.setBackgroundResource(R.drawable.right);
                                    isClicked[3] = true;
                                    left.setBackgroundResource(R.drawable.left_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            } else {
                                if (new State(getResources().getStringArray(R.array.States)[6]).modify()) {
                                    isClicked[3] = true;
                                    left.setBackgroundResource(R.drawable.left_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }
                        }
                    }
                } else {
                    if (!isClicked[0]) {
                        displayMessage(getString(R.string.TurnOnError), 1000);
                    } else {
                        if (!isClicked[1] && !isClicked[2]) {
                            displayMessage(getString(R.string.TurnLeftError), 1500);
                        }
                    }
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0] && (isClicked[1] || isClicked[2])) {
                    if (isClicked[4]) {
                        if (isClicked[1]) {
                            if (new State(getResources().getStringArray(R.array.States)[9]).modify()) {
                                isClicked[4] = false;
                                right.setBackgroundResource(R.drawable.right);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        } else {
                            if (new State(getResources().getStringArray(R.array.States)[11]).modify()) {
                                isClicked[4] = false;
                                right.setBackgroundResource(R.drawable.right);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                        }
                    } else {
                        if (isClicked[1]) {
                            if (isClicked[3]) {
                                if (new State(getResources().getStringArray(R.array.States)[8]).modify()) {
                                    isClicked[3] = false;
                                    left.setBackgroundResource(R.drawable.left);
                                    isClicked[4] = true;
                                    right.setBackgroundResource(R.drawable.right_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            } else {
                                if (new State(getResources().getStringArray(R.array.States)[8]).modify()) {
                                    isClicked[4] = true;
                                    right.setBackgroundResource(R.drawable.right_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }
                        }

                        if (isClicked[2]) {
                            if (isClicked[3]) {
                                if (new State(getResources().getStringArray(R.array.States)[10]).modify()) {
                                    isClicked[3] = false;
                                    left.setBackgroundResource(R.drawable.left);
                                    isClicked[4] = true;
                                    right.setBackgroundResource(R.drawable.right_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }  else {
                                if (new State(getResources().getStringArray(R.array.States)[10]).modify()) {
                                    isClicked[4] = true;
                                    right.setBackgroundResource(R.drawable.right_pressed);
                                } else {
                                    displayMessage(getString(R.string.Wait), 3000);
                                }
                            }
                        }

                    }
                } else {
                    if (!isClicked[0]) {
                        displayMessage(getString(R.string.TurnOnError), 1000);
                    } else {
                        if (!isClicked[1] && !isClicked[2]) {
                            displayMessage(getString(R.string.TurnRightError), 1500);
                        }
                    }
                }
            }
        });


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInStandBy) {
                    if (isClicked[0]) {
                        if (new State(getString(R.string.PlatformStop)).modify()) {
                            start.setBackgroundResource(R.drawable.start);
                            accelerate.setBackgroundResource(R.drawable.plus);
                            decelerate.setBackgroundResource(R.drawable.minus);
                            speed = 0;
                            isClicked[0] = false;
                            stopMove();
                            displayMessage(getString(R.string.PlatformOff), 500);
                        } else {
                            displayMessage(getString(R.string.Wait), 3000);
                        }
                    } else {
                        if (new State(getResources().getStringArray(R.array.Speeds)[0]).modify()) {
                            start.setBackgroundResource(R.drawable.start_speed1);
                            isClicked[0] = true;
                            speed = 1;
                            accelerate.setBackgroundResource(R.drawable.plus_pressed);
                            displayMessage(getString(R.string.PlatformOn), 500);
                        } else {
                            displayMessage(getString(R.string.Wait), 3000);
                        }
                    }
                } else {
                    displayMessage(getString(R.string.Connect), 4000);
                }
            }
        });

        accelerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0] && !isClicked[3] && !isClicked[4]) {
                    if (speed < 5)
                        speed++;
                    switch (speed) {
                        case 2:
                            if (new State(getResources().getStringArray(R.array.Speeds)[1]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed2);
                                decelerate.setBackgroundResource(R.drawable.minus_pressed);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 3:
                            if (new State(getResources().getStringArray(R.array.Speeds)[2]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed3);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 4:
                            if (new State(getResources().getStringArray(R.array.Speeds)[3]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed4);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 5:
                            if (new State(getResources().getStringArray(R.array.Speeds)[4]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed5);
                                accelerate.setBackgroundResource(R.drawable.plus);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                    }
                } else {
                    if (!isClicked[0]) {
                        displayMessage((getString(R.string.TurnOnError)), 1000);
                    } else {
                        if (isClicked[3] || isClicked[4]) {
                            displayMessage((getString(R.string.AccelerateError)), 1500);
                        }
                    }
                }
            }
        });

        decelerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked[0] && !isClicked[3] && !isClicked[4]) {
                    if (speed > 1)
                        speed--;
                    switch (speed) {
                        case 4:
                            if (new State(getResources().getStringArray(R.array.Speeds)[3]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed4);
                                accelerate.setBackgroundResource(R.drawable.plus_pressed);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 3:
                            if ( new State(getResources().getStringArray(R.array.Speeds)[2]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed3);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 2:
                            if ( new State(getResources().getStringArray(R.array.Speeds)[1]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed2);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                        case 1:
                            if ( new State(getResources().getStringArray(R.array.Speeds)[0]).modify()) {
                                start.setBackgroundResource(R.drawable.start_speed1);
                                decelerate.setBackgroundResource(R.drawable.minus);
                            } else {
                                displayMessage(getString(R.string.Wait), 3000);
                            }
                            break;
                    }
                } else {
                    if (!isClicked[0]) {
                        displayMessage(getString(R.string.TurnOnError), 1000);
                    } else {
                        if (isClicked[3] || isClicked[4]) {
                            displayMessage((getString(R.string.DecelerateError)), 1500);
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean value) {

    }

    @Override
    public void onPause() {
        if (new State(getString(R.string.PlatformStop)).modify()) {
            timer.cancel();
            super.onPause();
        } else {
            displayMessage(getString(R.string.Wait), 3000);
        }
    }

    @Override
    public void onBackPressed() {
        if (new State(getString(R.string.PlatformStop)).modify()) {
            timer.cancel();
            super.onBackPressed();
        } else {
            displayMessage(getString(R.string.Wait), 3000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stopMove() {
        if (isClicked[1]) {
            front.setBackgroundResource(R.drawable.front);
        }
        if (isClicked[2]) {
            back.setBackgroundResource(R.drawable.back);
        }
        if (isClicked[3]) {
            left.setBackgroundResource(R.drawable.left);
        }
        if (isClicked[4]) {
            right.setBackgroundResource(R.drawable.right);
        }
        for (int i = 1; i < isClicked.length; i++) {
            isClicked[i] = false;
        }
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(ActivityManualController.this, message, Toast.LENGTH_LONG);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    public class State {
        private String state;

        public State(String state) {
            this.state = state;
        }

        protected boolean modify() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null) {
                databaseDevices.child(deviceID).child("state").setValue(state);
                return true;
            } else {
                return false;
            }
        }
    }

    public class Alert extends TimerTask {
        private double distanceFront;
        private double distanceBack;

        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Device myDevice = dataSnapshot.getValue(Device.class);
                        distanceFront = myDevice.getDistanceFront();
                        distanceBack = myDevice.getDistanceBack();
                        if ((distanceFront < 50 || distanceBack < 50) && distanceFront >= 30 && distanceBack >= 30) {
                            for (int i = 50; i > 0; i--) {
                                if (distanceFront < i && distanceFront >= i - 5 || distanceBack < i && distanceBack >= i - 5) {
                                    Vibrator vibrator;
                                    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(VibrationEffect.createOneShot((51 - i) * 50, 255));
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

    public class AlertShow extends TimerTask {
        private double distanceFront;
        private double distanceBack;

        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Device myDevice = dataSnapshot.getValue(Device.class);
                        distanceFront = myDevice.getDistanceFront();
                        distanceBack = myDevice.getDistanceBack();
                        layout = findViewById(R.id.layout);
                        if ((distanceFront < 50 || distanceBack < 50) && distanceFront >= 30 && distanceBack >= 30) {
                            layout.setBackgroundResource(R.drawable.red_shadow);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    public class LayoutShow extends TimerTask {
        @Override
        public void run() {
            if (!isInStandBy) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout = findViewById(R.id.layout);
                        layout.setBackgroundResource(0);
                    }
                });
            }
        }
    }

    public class BlockedShow extends TimerTask {
        private double distanceFront;
        private double distanceBack;

        @Override
        public void run() {
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (deviceID != null && conManager.getActiveNetworkInfo() != null && !isInStandBy) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.DeviceDB)).child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Device myDevice = dataSnapshot.getValue(Device.class);
                        distanceFront = myDevice.getDistanceFront();
                        distanceBack = myDevice.getDistanceBack();
                        blockedPosition = myDevice.getBlocked();
                        if (distanceFront < 30 || distanceBack < 30) {
                            if (front.isClickable() && back.isClickable()) {
                                start.setBackgroundResource(R.drawable.start);
                                accelerate.setBackgroundResource(R.drawable.plus);
                                decelerate.setBackgroundResource(R.drawable.minus);
                                speed = 0;
                                isClicked[0] = false;
                                stopMove();
                                if (blockedPosition.equals("FRONT")) {
                                    displayMessage(getString(R.string.Blocked) + " in front of it", 2000);
                                } else {
                                    if (blockedPosition.equals("BACK")) {
                                        displayMessage(getString(R.string.Blocked) + " at the back of it", 2000);
                                    }
                                }

                            }
                            if (blockedPosition.equals("FRONT") && !isClicked[2]) {
                                front.setClickable(false);
                                front.setBackgroundResource(R.drawable.front_pressed);

                            } else {
                                if (blockedPosition.equals("BACK") && !isClicked[1])
                                back.setClickable(false);
                                back.setBackgroundResource(R.drawable.back_pressed);
                            }
                        } else {
                            if (!front.isClickable()) {
                                front.setClickable(true);
                                front.setBackgroundResource(R.drawable.front);
                                displayMessage(getString(R.string.Unlocked), 2000);
                            }

                            if (!back.isClickable()) {
                                back.setClickable(true);
                                back.setBackgroundResource(R.drawable.back);
                                displayMessage(getString(R.string.Unlocked), 2000);
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

    public class ButtonShow extends TimerTask {
        @Override
        public void run() {
            if (!isInStandBy) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (blockedPosition.equals("FRONT")) {
                            front.setBackgroundResource(R.drawable.front);
                        } else {
                            if (blockedPosition.equals("BACK")) {
                                back.setBackgroundResource(R.drawable.back);
                            }
                        }
                    }
                });
            }


        }
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


