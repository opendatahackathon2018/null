package ac00757.explorar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ac00757.explorar.utilities.ShowDetailsActivity;

import static ac00757.explorar.HomeActivity.MY_PREFS_NAME;

public class ARActivity extends Activity implements ArchitectJavaScriptInterfaceListener {

    //public static final String INTENT_EXTRAS_KEY_SAMPLE = "sampleData";
    private static final String TAG = ARActivity.class.getSimpleName();
    private static final String SAMPLES_ROOT = "poi/";
    protected ArchitectView architectView;
    private String arExperience;
    private String arNavigation;
    private String arOpenData;

    public static final String MY_PREFS_NAME = "preferences";
    private String arToLoad;
    private Double destinationLat;
    private Double destinationLong;

    private Double startLat;
    private Double startLong;

    private String firebaseUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //change experience on demand here
        arExperience = "poiassets/experience.html";
        arNavigation = "poiassets/navigation.html";
        arOpenData = "poiassets/opendata.html";

        WebView.setWebContentsDebuggingEnabled(true);

        /**
         * Configure the license key for the AR Library
         */
        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(getString(R.string.w_license_key));
        config.setCameraPosition(CameraSettings.CameraPosition.BACK);
        config.setCameraResolution(CameraSettings.CameraResolution.FULL_HD_1920x1080);
        config.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
        config.setCamera2Enabled(true);
        config.setFeatures(2);



        architectView = new ArchitectView(this);
        architectView.onCreate(config);
        architectView.addArchitectJavaScriptInterfaceListener(this);




        setContentView(architectView);


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        architectView.onPostCreate();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE); //open shared prefs to read destination - was set by clicking on a marker

        arToLoad = prefs.getString("arType", "no pref"); //"no pref" is the default value.

        destinationLat = Double.longBitsToDouble(prefs.getLong("destination-lat", Double.doubleToLongBits(0)));
        destinationLong = Double.longBitsToDouble(prefs.getLong("destination-lon", Double.doubleToLongBits(0)));
        startLat = Double.longBitsToDouble(prefs.getLong("start-lat", Double.doubleToLongBits(0)));
        startLong = Double.longBitsToDouble(prefs.getLong("start-lon", Double.doubleToLongBits(0)));


        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //firebaseUid = currentFirebaseUser.getUid();


        //Log.d("UID", firebaseUid);










        Log.d("dest lat", destinationLat.toString());
        Log.d("dest lon", destinationLong.toString());
        Log.d("start lat", startLat.toString());
        Log.d("start lon", startLong.toString());


        JSONObject latLng = new JSONObject();

        try {
            latLng.put("lat",destinationLat);
            latLng.put("lon",destinationLong);
            latLng.put("uid",firebaseUid);


        } catch (Exception e){
            e.printStackTrace();
        }

        JSONObject latLngStart = new JSONObject();

        try {
            latLngStart.put("lat",startLat);
            latLngStart.put("lon",startLong);
            latLngStart.put("uid",firebaseUid);

        } catch (Exception e){
            e.printStackTrace();
        }


        JSONArray jsonArray = new JSONArray();
        jsonArray.put(latLng);
        jsonArray.put(latLngStart);

        JSONObject destination = new JSONObject();

        try {
            destination.put("destination",jsonArray);
        } catch (JSONException e){
            e.printStackTrace();
        }






        JSONObject firebase = new JSONObject();

        /**
        try {
            firebase.put("uid", firebaseUid.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }**/




        architectView.callJavascript("World.getDataFromNative('"+destination+"');");
        //architectView.callJavascript("World.getFirebaseUid('"+firebase+"');");
        System.out.println(firebase);



        //pass these into the AR activity

        try {

            if (arToLoad.equals("experience")){

                architectView.load(SAMPLES_ROOT + arExperience);
            } else if (arToLoad.equals("navigation")){
                architectView.load(SAMPLES_ROOT + arNavigation);
            } else if (arToLoad.equals("opendata")){
                architectView.load(SAMPLES_ROOT + arOpenData);
            }

        } catch (IOException e) {
            Toast.makeText(this, "error loading ar experience", Toast.LENGTH_SHORT).show();
            //Log.e(TAG, "Exception while loading arExperience " + arExperience + ".", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        architectView.clearCache();
        architectView.removeArchitectJavaScriptInterfaceListener(this);
        architectView.onDestroy(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    public void onJSONObjectReceived(JSONObject jsonObject) {
        final Intent poiDetailIntent = new Intent(ARActivity.this, ShowDetailsActivity.class);
        try {
            switch (jsonObject.getString("action")) {
                case "present_poi_details":


                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q=" + jsonObject.get("title") ));
                    startActivity(browserIntent);
                    //poiDetailIntent.putExtra(ShowDetailsActivity.EXTRAS_KEY_POI_ID, jsonObject.getString("id"));
                    //poiDetailIntent.putExtra(ShowDetailsActivity.EXTRAS_KEY_POI_TITILE, jsonObject.getString("title"));
                    //poiDetailIntent.putExtra(ShowDetailsActivity.EXTRAS_KEY_POI_DESCR, jsonObject.getString("description"));

                    //this.startActivity(poiDetailIntent);
                    break;
            }

        } catch (JSONException e) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ARActivity.this, R.string.error_parsing_json, Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();
        }
    }


}
