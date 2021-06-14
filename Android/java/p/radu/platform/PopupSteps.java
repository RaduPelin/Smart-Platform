package p.radu.platform;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.parceler.Parcels;

import java.util.List;

public class PopupSteps extends AppCompatActivity {
    //defining view objects
    private List<Step> newSteps;
    private String programName;
    private String canClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_steps);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.8), (int) (height *.6));

        Intent takeSteps = getIntent();
        Bundle bundle = takeSteps.getBundleExtra("STEPS_EXTRA");

        if (bundle != null) {
            newSteps = Parcels.unwrap(bundle.getParcelable("STEPS_PARCEL"));
        }
        String close = takeSteps.getStringExtra("CAN_CLOSE");

        if (close != null) {
            canClose = close;
        }

        String program = takeSteps.getStringExtra("PROGRAM_NAME");

        if (program != null) {
            programName = program;
        }

        if (newSteps != null) {
            showSteps(newSteps);
        }
    }

    public void showSteps(final List<Step> newSteps) {
        LinearLayout layout = findViewById(R.id.scrollViewStepsLayout);
        layout.removeAllViews();
        ScrollView viewSteps = findViewById(R.id.scrollViewSteps);
        viewSteps.removeAllViews();
        viewSteps.addView(layout);
        for (int i = 0; i< newSteps.size(); i++) {
            Step step = newSteps.get(i);
            String title = "Step " + (i+1);
            String direction = "Direction: " +  step.getDirection();
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
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) titleView.getLayoutParams();
            layoutParams.setMargins(250, 0 , 250, 0);
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
            layoutParams.setMargins(250, 0 , 250, 0);
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
            layoutParams.setMargins(250, 0 , 250, 0);
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
            layoutParams.setMargins(250, 0 , 250, 0);
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
            layoutParams.setMargins(250, 0 , 250, 0);
            delayView.setLayoutParams(layoutParams);

            LinearLayout buttonsLayout = new LinearLayout(this);
            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

            final ImageButton remove = new ImageButton(this);
            final ImageButton edit = new ImageButton(this);

            remove.setBackgroundResource(R.drawable.delete);
            edit.setBackgroundResource(R.drawable.edit);

            buttonsLayout.addView(remove);

            layoutParams = (LinearLayout.LayoutParams) remove.getLayoutParams();
            layoutParams.setMargins(0, 0 , 50, 20);
            remove.setLayoutParams(layoutParams);
            remove.setContentDescription("Step" + (i+1));

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < newSteps.size(); j++) {
                        if (remove.getContentDescription().equals("Step" + (j+1))) {
                            Intent assurance = new Intent(PopupSteps.this, PopupAssuranceDeleteStep.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
                            assurance.putExtra("STEPS_EXTRA", bundle);
                            assurance.putExtra("PROGRAM_NAME", programName);
                            assurance.putExtra("STEP", "Step" + (j+1));
                            assurance.putExtra("MESSAGE", getResources().getStringArray(R.array.RemoveAssurance)[0]);
                            canClose = "No";
                            startActivity(assurance);
                        }
                    }
                }
            });

            buttonsLayout.addView(edit);

            layoutParams = (LinearLayout.LayoutParams) edit.getLayoutParams();
            layoutParams.setMargins(0, 10 , 0, 20);
            edit.setLayoutParams(layoutParams);
            edit.setContentDescription("Step" + (i+1));

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < newSteps.size(); j++) {
                        if (edit.getContentDescription().equals("Step" + (j+1))) {
                            Intent editStep = new Intent(PopupSteps.this, PopupEditStep.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("EDIT_STEPS_PARCEL", Parcels.wrap(newSteps));
                            editStep.putExtra("EDIT_STEPS_EXTRA", bundle);
                            editStep.putExtra("PROGRAM_NAME", programName);
                            editStep.putExtra("STEP", "Step" + (j+1));
                            canClose = "No";
                            startActivity(editStep);
                        }

                    }
                }

            });


        layout.addView(buttonsLayout);
            layoutParams = (LinearLayout.LayoutParams) buttonsLayout.getLayoutParams();
            layoutParams.setMargins(700, 50 , 0, 0);
            buttonsLayout.setLayoutParams(layoutParams);

        }
    }

    @Override
    public void onBackPressed() {
        if (canClose.equals("Yes")) {
            Intent addProgram = new Intent(PopupSteps.this, ActivityAutomatedControlCreate.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
            addProgram.putExtra("STEPS_EXTRA", bundle);
            addProgram.putExtra("PROGRAM_NAME", programName);
            finishAffinity();
            startActivity(addProgram);
        }
        super.onBackPressed();


    }

    @Override
    public void onPause() {
        if (canClose.equals("Yes")) {
            Intent addProgram = new Intent(PopupSteps.this, ActivityAutomatedControlCreate.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
            addProgram.putExtra("STEPS_EXTRA", bundle);
            addProgram.putExtra("PROGRAM_NAME", programName);
            finishAffinity();
            startActivity(addProgram);
        }

        super.onPause();
    }

}
