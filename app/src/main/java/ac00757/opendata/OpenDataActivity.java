package ac00757.explorar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


public class OpenDataActivity extends Activity {

    public static final String MY_PREFS_NAME = "preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_data);


        Intent goToLiveActivity = new Intent(OpenDataActivity.this,GeoARActivity.class);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("arType", "opendata");
        editor.apply();

        startActivity(goToLiveActivity);
    }
}
