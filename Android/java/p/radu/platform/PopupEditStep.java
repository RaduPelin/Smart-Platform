package p.radu.platform;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.parceler.Parcels;

import java.util.List;

public class PopupEditStep extends AppCompatActivity {
    //defining view objects
    private Spinner spinnerDirection;
    private Spinner spinnerDistance;
    private Spinner spinnerDelay;
    private EditText editTextDistance;
    private EditText editTextNumberOfLapses;
    private EditText editTextDelay;
    private Button buttonCancel;
    private Button buttonDone;
    private String programName;
    private List<Step> editableSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_step);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.65), (int) (height *.45));

        final Intent takeEditableSteps = getIntent();

        Bundle bundle = takeEditableSteps.getBundleExtra("EDIT_STEPS_EXTRA");

        if (bundle != null) {
            editableSteps = Parcels.unwrap(bundle.getParcelable("EDIT_STEPS_PARCEL"));

            for (int i = 0; i < editableSteps.size(); i++) {
                if ((takeEditableSteps.getStringExtra("STEP")).equals(("Step" + (i+1)))) {
                    final ArrayAdapter<String> adapterDirection = new ArrayAdapter<String>(this,R.layout.spinner_small, getResources().getStringArray(R.array.Directions)) {
                        @Override
                        public int getCount() {
                            return getResources().getStringArray(R.array.Directions).length - 1; // Truncate the list
                        }
                    };

                    final ArrayAdapter<String> adapterDistance = new ArrayAdapter<String>(this,R.layout.spinner_small, getResources().getStringArray(R.array.Distances)) {
                        @Override
                        public int getCount() {
                            return getResources().getStringArray(R.array.Distances).length - 1; // Truncate the list
                        }
                    };

                    final ArrayAdapter<String> adapterDelay = new ArrayAdapter<String>(this,R.layout.spinner_small, getResources().getStringArray(R.array.Delays)) {
                        @Override
                        public int getCount() {
                            return getResources().getStringArray(R.array.Delays).length - 1; // Truncate the list
                        }
                    };

                    String direction = editableSteps.get(i).getDirection();


                    adapterDirection.setDropDownViewResource(R.layout.spinner_dropdown_small);
                    spinnerDirection = findViewById(R.id.spinnerEditDirection);
                    spinnerDirection.setAdapter(adapterDirection);

                    for (int j = 0; j < getResources().getStringArray(R.array.Directions).length; j++) {
                        if (getResources().getStringArray(R.array.Directions)[j].equals(direction)) {
                            spinnerDirection.setSelection(j);
                        }
                    }

                    String distanceUnits = editableSteps.get(i).getDistanceUnits();

                    adapterDistance.setDropDownViewResource(R.layout.spinner_dropdown_small);
                    spinnerDistance = findViewById(R.id.spinnerEditDistance);
                    spinnerDistance.setAdapter(adapterDistance);

                    for (int j = 0; j < getResources().getStringArray(R.array.Distances).length; j++) {
                        if (getResources().getStringArray(R.array.Distances)[j].equals(distanceUnits)) {
                            spinnerDistance.setSelection(j);
                        }
                    }

                    String delayUnits = editableSteps.get(i).getDelayUnits();

                    adapterDelay.setDropDownViewResource(R.layout.spinner_dropdown_small);

                    spinnerDelay = findViewById(R.id.spinnerEditDelay);
                    spinnerDelay.setAdapter(adapterDelay);

                    for (int j = 0; j < getResources().getStringArray(R.array.Delays).length; j++) {
                        if (getResources().getStringArray(R.array.Delays)[j].equals(delayUnits)) {
                            spinnerDelay.setSelection(j);
                        }
                    }

                    editTextDistance = findViewById(R.id.editTextEditDistance);
                    editTextDistance.setText(""+editableSteps.get(i).getDistance());

                    editTextNumberOfLapses = findViewById(R.id.editTextEditNumberOfLapses);
                    editTextNumberOfLapses.setText(""+editableSteps.get(i).getNumberOfLapses());
                    editTextDelay = findViewById(R.id.editTextEditDelay);
                    editTextDelay.setText(""+editableSteps.get(i).delay);
                }
            }
        }

        String program  = takeEditableSteps.getStringExtra("PROGRAM_NAME");
        if (program != null) {
            programName = program;

        }



        buttonCancel = findViewById(R.id.buttonCancel);
        buttonDone = findViewById(R.id.buttonDone);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String direction = spinnerDirection.getSelectedItem().toString();
                String distanceUnits  = spinnerDistance.getSelectedItem().toString();
                String delayUnits = spinnerDelay.getSelectedItem().toString();
                String distance = editTextDistance.getText().toString();
                String lapses = editTextNumberOfLapses.getText().toString();
                String delay = editTextDelay.getText().toString();
                double distanceValue;
                int numberOfLapses;
                double delayValue;


                if (TextUtils.isEmpty(distance)) {
                    displayMessage(getString(R.string.DistanceError), 2000);
                    return;
                } else {
                    if (!distance.matches("^\\d*\\.?\\d*$")) {
                        displayMessage(getString(R.string.InvalidDistanceError), 2000);
                        return;
                    }  else {
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
                    displayMessage(getString(R.string.LapseError), 2000);
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

                if (TextUtils.isEmpty(delay)) {
                    displayMessage(getString(R.string.DelayError), 2000);
                    return;
                } else {
                    if (!delay.matches("^\\d*\\.?\\d*$")) {
                        displayMessage(getString(R.string.InvalidDelayError), 2000);
                        return;
                    } else {
                        if (Double.parseDouble(delay) > 59.99 && delayUnits.equals(getResources().getStringArray(R.array.Delays)[0])) {
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

                Step stepEdited = new Step(direction, distanceValue, distanceUnits, numberOfLapses, delayValue, delayUnits);

                for (int i = 0; i < editableSteps.size(); i++) {
                    if (takeEditableSteps.getStringExtra("STEP").equals(("Step" + (i+1)))) {
                        editableSteps.set(i,stepEdited);

                        displayMessage(getString(R.string.Edited), 1500);
                    }
                }

                Intent viewSteps = new Intent(PopupEditStep.this, PopupSteps.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(editableSteps));
                viewSteps.putExtra("STEPS_EXTRA", bundle);
                viewSteps.putExtra("PROGRAM_NAME", programName);
                viewSteps.putExtra("CAN_CLOSE", "Yes");
                finishAfterTransition();
                startActivity(viewSteps);

            }
        });


    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupEditStep.this, message, Toast.LENGTH_LONG);
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
