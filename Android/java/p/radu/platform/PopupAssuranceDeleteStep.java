package p.radu.platform;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.List;

public class PopupAssuranceDeleteStep extends AppCompatActivity {
    //defining view objects
    private Button buttonYes;
    private Button buttonNo;
    private TextView textViewAssurance;

    //defining privates
    private List<Step> newSteps;
    private String programName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_assurance);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.65), (int) (height *.35));

        final Intent takeSteps = getIntent();

        Bundle bundle = takeSteps.getBundleExtra("STEPS_EXTRA");

        if (bundle != null) {
            newSteps = Parcels.unwrap(bundle.getParcelable("STEPS_PARCEL"));
        }

        String program = takeSteps.getStringExtra("PROGRAM_NAME");

        if (program != null) {
            programName = program;
        }

        textViewAssurance = findViewById(R.id.textViewAssurance);
        textViewAssurance.setText(takeSteps.getStringExtra("MESSAGE"));

        buttonYes = findViewById(R.id.buttonOk);
        buttonNo = findViewById(R.id.buttonNo);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewSteps = new Intent(PopupAssuranceDeleteStep.this, PopupSteps.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
                viewSteps.putExtra("STEPS_EXTRA", bundle);
                viewSteps.putExtra("PROGRAM_NAME", programName);
                finishAfterTransition();
                startActivity(viewSteps);
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < newSteps.size(); i++) {
                    if (takeSteps.getStringExtra("STEP").equals("Step" + (i+1))) {
                        newSteps.remove(i);
                        if (newSteps.size() > 0) {
                            Intent viewSteps = new Intent(PopupAssuranceDeleteStep.this, PopupSteps.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
                            viewSteps.putExtra("STEPS_EXTRA", bundle);
                            viewSteps.putExtra("PROGRAM_NAME", programName);
                            viewSteps.putExtra("CAN_CLOSE", "Yes");
                            finishAfterTransition();
                            startActivity(viewSteps);
                        } else {
                            Intent addProgram = new Intent(PopupAssuranceDeleteStep.this, ActivityAutomatedControlCreate.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("STEPS_PARCEL", Parcels.wrap(newSteps));
                            addProgram.putExtra("STEPS_EXTRA", bundle);
                            addProgram.putExtra("PROGRAM_NAME", programName);
                            finishAffinity();
                            startActivity(addProgram);
                        }

                    }
                }
            }
        });
    }
}
