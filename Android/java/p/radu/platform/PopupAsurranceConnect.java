package p.radu.platform;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PopupAsurranceConnect extends AppCompatActivity {
    //defining view objects
    private Button buttonOk;
    private Button buttonCancel;
    private TextView textViewAssurance;

    //defining privates
    private boolean isConnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_assurance);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width =  displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width *.65), (int) (height *.35));

        Intent connected = getIntent();

        isConnecting = connected.getBooleanExtra("CONNECT", false);

        textViewAssurance = findViewById(R.id.textViewAssurance);
        textViewAssurance.setText(getString(R.string.PluggedIn));

        buttonOk = findViewById(R.id.buttonOk);
        buttonOk.setText("OK");
        buttonCancel = findViewById(R.id.buttonNo);
        buttonCancel.setText("CANCEL");

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null) {
                    Intent settings = new Intent(PopupAsurranceConnect.this, ActivitySettings.class);
                    settings.putExtra("CONNECT", isConnecting);
                    finishAffinity();
                    startActivity(settings);
                } else {
                    displayMessage(getString(R.string.Wait), 3000);
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settings = new Intent(PopupAsurranceConnect.this, ActivitySettings.class);
                settings.putExtra("CONNECT", false );
                finishAffinity();
                startActivity(settings);
            }
        });
    }

    private void displayMessage(String message, int duration) {
        final Toast toast = Toast.makeText(PopupAsurranceConnect.this, message, Toast.LENGTH_LONG);
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
